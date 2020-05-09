package com.hongframe.raft.core;

import com.hongframe.raft.Status;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.option.ReplicatorOptions;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.util.ObjectLock;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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

    private FlyingAppendEntries fiying;
    private ArrayDeque<FlyingAppendEntries> appendEntriesInFly = new ArrayDeque<>();
    private ScheduledFuture<?> heartbeatTimer;
    private int reqSeq = 0;
    private int requiredNextSeq = 0;
    private final PriorityQueue<RpcResponse> pendingResponses = new PriorityQueue<>(50);

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

    private class FlyingAppendEntries {
        final long startLogIndex;
        final int entriesSize;
        final int seq;
        final CompletableFuture<?> future;

        public FlyingAppendEntries(long startLogIndex, int entriesSize, int seq, CompletableFuture<?> future) {
            this.startLogIndex = startLogIndex;
            this.entriesSize = entriesSize;
            this.seq = seq;
            this.future = future;
        }

        boolean isSendingLogEntries() {
            return this.entriesSize > 0;
        }
    }

    private class RpcResponse implements Comparable<RpcResponse> {
        final Status status;
        final Message request;
        final Message response;
        final long rpcSendTime;
        final int seq;

        private RpcResponse(Status status, Message request, Message response, long rpcSendTime, int seq) {
            this.status = status;
            this.request = request;
            this.response = response;
            this.rpcSendTime = rpcSendTime;
            this.seq = seq;
        }

        @Override
        public int compareTo(RpcResponse o) {
            return Integer.compare(this.seq, o.seq);
        }
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

    private FlyingAppendEntries pollInFly() {
        return this.appendEntriesInFly.poll();
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
                        onAppendEntriesReturned(Replicator.this.self, status, request, (AppendEntriesResponse) getResponse(), reqSeq, monotonicSendTimeMs);
                    }
                });
                addFlying(0, 0, reqSeq, future);
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

    private void onAppendEntriesReturned(ObjectLock<Replicator> lock, Status status, AppendEntriesRequest request,
                                         AppendEntriesResponse response, int seq, long monotonicSendTimeMs) {
        Replicator replicator = lock.lock();

        boolean continueSendEntries = true;
        try {
            final PriorityQueue<RpcResponse> holdingQueue = replicator.pendingResponses;
            holdingQueue.add(new RpcResponse(status, request, response, monotonicSendTimeMs, seq));
            if (holdingQueue.size() > this.options.getRaftOptions().getMaxReplicatorFlyingMsgs()) {
                replicator.sendEmptyEntries(false);
                return;
            }

            int processed = 0;
            while (!holdingQueue.isEmpty()) {
                RpcResponse rpcResponse = holdingQueue.peek();

                if (rpcResponse.seq != replicator.requiredNextSeq) {
                    if (processed > 0) {
                        break;
                    }
                    continueSendEntries = false;
//                    lock.unlock();
                    return;
                }

                holdingQueue.remove();
                processed++;
                FlyingAppendEntries flying = replicator.pollInFly();
                if (flying == null) {
                    continue;
                }
                if (flying.seq != rpcResponse.seq) {
                    //TODO 不知道什么情况下会这样 and block
                }

                try {
                    request = (AppendEntriesRequest) rpcResponse.request;
                    response = (AppendEntriesResponse) rpcResponse.response;
                    if (flying.startLogIndex != request.getPreLogIndex() + 1) {
                        //TODO
                        continueSendEntries = false;
                        break;
                    }

                    if (!status.isOk()) {
                        //TODO block
                        continueSendEntries = false;
                        break;
                    }

                    if (!response.getSuccess()) {
                        if (response.getTerm() > replicator.options.getTerm()) {
                            //TODO dowm step
                            continueSendEntries = false;
                            break;
                        }
                        if (monotonicSendTimeMs > replicator.lastRpcSendTimestamp) {
                            replicator.lastRpcSendTimestamp = monotonicSendTimeMs;
                        }
                        //TODO appendEntriesInFly clear

                        if (response.getLastLogLast() + 1 < replicator.nextIndex) {
                            replicator.nextIndex = response.getLastLogLast() + 1;
                        } else {
                            if (replicator.nextIndex > 1) {
                                replicator.nextIndex--;
                            }
                        }
                        replicator.sendEmptyEntries(false);
                        continueSendEntries = false;
                        break;
                    }

                    if (response.getTerm() != replicator.options.getTerm()) {
                        //TODO appendEntriesInFly clear
                        continueSendEntries = false;
                        break;
                    }

                    if (monotonicSendTimeMs > replicator.lastRpcSendTimestamp) {
                        replicator.lastRpcSendTimestamp = monotonicSendTimeMs;
                    }
                    if (request.getEntriesCount() > 0) {
                        replicator.options.getBallotBox().commitAt(replicator.nextIndex,
                                replicator.nextIndex + request.getEntriesCount() - 1, replicator.options.getPeerId());
                    } else {
                        replicator.state = State.Replicate;
                    }
                    replicator.nextIndex += request.getEntriesCount();
                    continueSendEntries = true;
                } finally {
                    //TODO
                    if (continueSendEntries) {
                        replicator.getAndIncrementRequiredNextSeq();
                    }
                }

            }

        } finally {

            if (continueSendEntries) {
                // TODO send entries
                replicator.sendEntries();
            } else {
                lock.unlock();
            }
        }
    }

    private void addFlying(long startLogIndex, int entriesSize, int seq, CompletableFuture future) {
        this.fiying = new FlyingAppendEntries(startLogIndex, entriesSize, seq, future);
        this.appendEntriesInFly.add(fiying);
    }

    private int getNextSendIndex() {
        if (this.appendEntriesInFly.isEmpty()) {
            return -1;
        }
        if (this.appendEntriesInFly.size() > this.options.getRaftOptions().getMaxReplicatorFlyingMsgs()) {
            return -1;
        }
        if (this.fiying != null && this.fiying.isSendingLogEntries()) {
            return (int) this.fiying.startLogIndex + this.fiying.entriesSize;
        }
        return -1;
    }

    private void sendEntries() {
        long prevSendIndex = -1;
        while (true) {
            long nextSendIndex = getNextSendIndex();
            if (nextSendIndex > prevSendIndex) {
                if (sendEntries(nextSendIndex)) {
                    prevSendIndex = nextSendIndex;
                } else {
                    break;
                }
            } else {
                break;
            }

        }
    }

    private boolean sendEntries(final long nextSendingIndex) {
        AppendEntriesRequest request = new AppendEntriesRequest();
        request.setTerm(this.options.getTerm());
        request.setServerId(this.options.getServerId().toString());
        request.setGroupId(this.options.getGroupId());
        request.setPeerId(this.options.getPeerId().toString());
        request.setPreLogIndex(nextSendingIndex - 1);
        request.setPrevLogTerm(this.options.getLogManager().getTerm(nextSendingIndex - 1));
        request.setCommittedIndex(this.options.getBallotBox().getLastCommittedIndex());

        final int maxEntriesSize = this.options.getRaftOptions().getMaxEntriesSize();
        List<LogEntry> entries = new LinkedList<>();
        for (int i = 0; i < maxEntriesSize; i++) {
            if (!prepareEntry(nextSendingIndex, i, entries)) {
                break;
            }
        }
        if (entries.isEmpty()) {
            //TODO wait more entries
            return false;
        }
        request.setEntries(entries);
        //TODO send request
        final long monotonicSendTimeMs = Utils.monotonicMs();
        final int seq = getAndIncrementReqSeq();
        CompletableFuture future = this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
            @Override
            public void run(Status status) {
                onAppendEntriesReturned(Replicator.this.self, status, request, (AppendEntriesResponse) getResponse(), seq, monotonicSendTimeMs);
            }
        });
        addFlying(nextSendingIndex, entries.size(), seq, future);
        return true;
    }

    private boolean prepareEntry(long nextSendIndex, int offset, List<LogEntry> entries) {
        long logIndex = nextSendIndex + offset;
        LogEntry entry = this.options.getLogManager().getEntry(logIndex);
        if (entry == null) {
            return false;
        }
        entries.add(entry);
        return true;
    }

    public static long getLastRpcSendTimestamp(final ObjectLock<Replicator> lock) {
        final Replicator r = lock.getData();
        if (r == null) {
            return 0L;
        }
        return r.lastRpcSendTimestamp;
    }

}
