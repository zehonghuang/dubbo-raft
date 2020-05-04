package com.hongframe.raft.core;

import com.hongframe.raft.Status;
import com.hongframe.raft.option.ReplicatorOptions;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.util.ObjectLock;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Replicator {

    private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

    private RpcClient rpcClient;
    private volatile long nextIndex = 1;
    private State state;
    private ObjectLock<Replicator> self;
    private final ReplicatorOptions options;
    private Scheduler timerManger;
    private volatile long lastRpcSendTimestamp;
    private volatile long heartbeatCounter = 0;
    private int reqSeq = 0;
    private int requiredNextSeq = 0;

    private ScheduledFuture<?> heartbeatTimer;

    private Replicator(ReplicatorOptions options) {
        this.options = options;
        this.rpcClient = this.options.getRpcClient();
        this.timerManger = this.options.getTimerManager();

    }

    public enum State {
        Probe,
        Replicate,
        Destroyed;
    }

    private int getAndIncrementReqSeq() {
        final int prev = this.reqSeq;
        this.reqSeq++;
        if (this.reqSeq < 0) {
            this.reqSeq = 0;
        }
        return prev;
    }

    private int getAndIncrementRequiredNextSeq() {
        final int prev = this.requiredNextSeq;
        this.requiredNextSeq++;
        if (this.requiredNextSeq < 0) {
            this.requiredNextSeq = 0;
        }
        return prev;
    }


    public static ObjectLock<Replicator> start(ReplicatorOptions options) {
        Replicator replicator = new Replicator(options);

        if (!replicator.rpcClient.connect(replicator.options.getPeerId())) {
            return null;
        }
        LOG.info("start Replicator :{}", replicator.options.getPeerId());
        ObjectLock<Replicator> lock = new ObjectLock<>(replicator);
        replicator.self = lock;
        lock.lock();
        replicator.lastRpcSendTimestamp = Utils.monotonicMs();
        replicator.startHeartbeatTimer(Utils.nowMs());
        lock.unlock();
        return lock;
    }

    private void startHeartbeatTimer(long startMs) {
        final long dueTime = startMs + this.options.getDynamicHeartBeatTimeoutMs();
        this.heartbeatTimer = this.timerManger.schedule(() -> onTimeout(this.self), dueTime - Utils.nowMs(), TimeUnit.MILLISECONDS);
    }

    private void onTimeout(ObjectLock<Replicator> lock) {
        Utils.runInThread(() -> sendHeartbeat(lock));
    }

    private static void sendHeartbeat(final ObjectLock<Replicator> lock) {
        final Replicator r = lock.lock();
        if (r == null) {
            return;
        }
        // unlock in sendEmptyEntries
        r.sendEmptyEntries(true);
    }

    private void sendEmptyEntries(final boolean isHeartbeat) {
        try {
            AppendEntriesRequest request = new AppendEntriesRequest();
            long prevLogTerm = this.options.getLogManager().getTerm(this.nextIndex - 1);
            request.setTerm(this.options.getTerm());
            request.setGroupId(this.options.getGroupId());
            request.setServerId(this.options.getServerId().toString());
            request.setPeerId(this.options.getPeerId().toString());
            request.setPrevLogTerm(prevLogTerm);
            request.setPreLogIndex(this.nextIndex - 1);
            request.setCommittedIndex(this.options.getBallotBox().getLastCommittedIndex());

            final long monotonicSendTimeMs = Utils.monotonicMs();

            if (isHeartbeat) {
                this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        onHeartbeatReturned(Replicator.this.self, status, (AppendEntriesResponse) getResponse(), monotonicSendTimeMs);
                    }
                });
            } else {
                this.state = State.Probe;
                int reqSeq = getAndIncrementReqSeq();
                CompletableFuture<?> future = this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        //TODO appendEntries response
                        onAppendEntriesReturned(Replicator.this.self, status, (AppendEntriesResponse) getResponse(), monotonicSendTimeMs);
                    }
                });
            }
        } finally {
            this.self.unlock();
        }
    }

    private void onHeartbeatReturned(ObjectLock<Replicator> lock, Status status, AppendEntriesResponse response, long monotonicSendTimeMs) {

        boolean doUnlock = true;
        final long startTimeMs = Utils.nowMs();
        Replicator replicator = lock.lock();

        try {
            if (!status.isOk()) {
                LOG.warn("onHeartbeatReturned {}", status.getErrorMsg());
                replicator.startHeartbeatTimer(startTimeMs);
                return;
            }
            if (response.getTerm() > replicator.options.getTerm()) {
                //TODO down step
                return;
            }
            if (!response.getSuccess() && !(response.getLastLogLast() < 0)) {
                doUnlock = false;
                replicator.sendEmptyEntries(false);
                replicator.startHeartbeatTimer(startTimeMs);
                return;
            }
            if (monotonicSendTimeMs > replicator.lastRpcSendTimestamp) {
                replicator.lastRpcSendTimestamp = monotonicSendTimeMs;
            }
            replicator.startHeartbeatTimer(startTimeMs);
        } finally {
            if (doUnlock) {
                lock.unlock();
            }

        }
    }

    private void onAppendEntriesReturned(ObjectLock<Replicator> lock, Status status, AppendEntriesResponse response, long monotonicSendTimeMs) {

    }

}
