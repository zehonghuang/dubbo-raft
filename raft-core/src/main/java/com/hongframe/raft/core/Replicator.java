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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Replicator {

    private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

    private RpcClient rpcClient;
    private volatile long nextIndex = 1;
    private ObjectLock<Replicator> self;
    private final ReplicatorOptions options;
    private Scheduler timerManger;
    private volatile long lastRpcSendTimestamp;
    private volatile long heartbeatCounter = 0;

    private ScheduledFuture<?> heartbeatTimer;

    private Replicator(ReplicatorOptions options) {
        this.options = options;
        this.rpcClient = this.options.getRpcClient();
        this.timerManger = this.options.getTimerManager();

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
            request.setPreLogIndex(prevLogTerm);
            request.setCommittedIndex(0l);

            final long monotonicSendTimeMs = Utils.monotonicMs();

            if (isHeartbeat) {
                this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        onHeartbeatReturned(status, (AppendEntriesResponse) getResponse(), monotonicSendTimeMs);
                    }
                });
            }
        } finally {
            this.self.unlock();
        }
    }

    private void onHeartbeatReturned(Status status, AppendEntriesResponse response, long monotonicSendTimeMs) {
        if(!status.isOk()) {
            //TODO block
            LOG.warn("onHeartbeatReturned {}", status.getErrorMsg());
        }
        onTimeout(this.self);
    }

}
