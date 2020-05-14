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
import java.util.concurrent.atomic.AtomicBoolean;

public class Replicator {

    private Logger LOG;

    private RpcClient rpcClient;
    private volatile long nextIndex = 1;
    private State state;
    private long waitId = -1L;
    private ObjectLock<Replicator> self;
    private final ReplicatorOptions options;
    private Scheduler timerManger;
    private volatile long lastRpcSendTimestamp;
    private volatile long heartbeatCounter = 0;

    private FlyingAppendEntries fiying;
    private ArrayDeque<FlyingAppendEntries> appendEntriesInFly = new ArrayDeque<>();
    private CompletableFuture<?> heartbeatInFly;
    private ScheduledFuture<?> heartbeatTimer;
    private int reqSeq = 0;
    private int requiredNextSeq = 0;
    private final PriorityQueue<RpcResponse> pendingResponses = new PriorityQueue<>(50);

    private Replicator(ReplicatorOptions options) {
        this.options = options;
        this.rpcClient = this.options.getRpcClient();
        this.timerManger = this.options.getTimerManager();
        this.LOG = LoggerFactory.getLogger(Replicator.class.getSimpleName() + " : " + this.options.getPeerId());
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

        @Override
        public String toString() {
            return "FlyingAppendEntries{" +
                    "startLogIndex=" + startLogIndex +
                    ", entriesSize=" + entriesSize +
                    ", seq=" + seq +
                    ", future=" + future +
                    '}';
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
        public String toString() {
            return "RpcResponse{" +
                    "status=" + status +
                    ", request=" + request +
                    ", response=" + response +
                    ", rpcSendTime=" + rpcSendTime +
                    ", seq=" + seq +
                    '}';
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

        ObjectLock<Replicator> lock = new ObjectLock<>(replicator);
        replicator.self = lock;
        lock.lock();
        replicator.lastRpcSendTimestamp = Utils.monotonicMs();
        replicator.startHeartbeatTimer(Utils.nowMs());
        replicator.LOG.warn("startHeartbeatTimer");
        replicator.sendEmptyEntries(false);
        replicator.LOG.info("start Replicator :{}", replicator.options.getPeerId());
//        lock.unlock();
        return lock;
    }

    private void startHeartbeatTimer(long startMs) {
        final long dueTime = startMs + this.options.getDynamicHeartBeatTimeoutMs();
        long delay = dueTime - Utils.nowMs();
        this.heartbeatTimer = this.timerManger.schedule(() -> onTimeout(this.self), delay, TimeUnit.MILLISECONDS);
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
        final AtomicBoolean doUnlock = new AtomicBoolean(true);
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
                this.heartbeatInFly = this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        AppendEntriesResponse appendEntriesResponse = null;
                        if (!status.isOk()) {
                            doUnlock.set(false);
                            Replicator.this.self.unlock();
                        } else {
                            appendEntriesResponse = (AppendEntriesResponse) getResponse();
                        }
                        onHeartbeatReturned(Replicator.this.self, status, appendEntriesResponse, monotonicSendTimeMs);
                    }
                });
            } else {
                this.state = State.Probe;
                int reqSeq = getAndIncrementReqSeq();
                CompletableFuture<?> future = this.rpcClient.appendEntries(this.options.getPeerId(), request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        //TODO appendEntries response
                        AppendEntriesResponse appendEntriesResponse = null;
                        if (!status.isOk()) {
                            doUnlock.set(false);
                            Replicator.this.self.unlock();
                        } else {
                            appendEntriesResponse = (AppendEntriesResponse) getResponse();
                        }
                        onAppendEntriesReturned(Replicator.this.self, status, request, appendEntriesResponse, reqSeq, monotonicSendTimeMs);
                    }
                });
                if(future != null) {
                    addFlying(this.nextIndex, 0, reqSeq, future);
                }

            }
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            if (doUnlock.get()) {
                this.self.unlock();
            }

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
        boolean doUnlock = true;
        Replicator replicator = lock.lock();
        LOG.info("replicator state is {}", this.state);
        boolean continueSendEntries = true;
        if(this.options.getPeerId().getPort() == 8890) {
            LOG.warn("\n{}\n{}", request, response);
        }

        try {
            final PriorityQueue<RpcResponse> holdingQueue = replicator.pendingResponses;
            holdingQueue.add(new RpcResponse(status, request, response, monotonicSendTimeMs, seq));
            LOG.info("pendingResponses size: {}", holdingQueue.size());
            if (holdingQueue.size() > this.options.getRaftOptions().getMaxReplicatorFlyingMsgs()) {
                LOG.info("pendingResponses size: {} more than Max Replicator Flying Msgs: {}", holdingQueue.size(),
                        this.options.getRaftOptions().getMaxReplicatorFlyingMsgs());
                doUnlock = false;
                continueSendEntries = false;
                replicator.resetInflights();
                replicator.sendEmptyEntries(false);
                return;
            }

            if (!status.isOk()) {
                //TODO block
                LOG.warn("onAppendEntriesReturned status :{}", (status.isOk() ? "OK!" : "Not OK!!!"));
                continueSendEntries = false;
                doUnlock = false;
                replicator.resetInflights();
                replicator.sendEmptyEntries(false);
                return;
            }

            int processed = 0;
            while (!holdingQueue.isEmpty()) {
                RpcResponse rpcResponse = holdingQueue.peek();

                if (rpcResponse.seq != replicator.requiredNextSeq) {
                    LOG.info("request seq illegal : seq {}, required {}", rpcResponse.seq, replicator.requiredNextSeq);
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
                LOG.warn("replicator.appendEntriesInFly: {}", replicator.appendEntriesInFly);
                if (flying.seq != rpcResponse.seq) {
                    //TODO 不知道什么情况下会这样 and block
                }

                try {
                    request = (AppendEntriesRequest) rpcResponse.request;

                    if (flying.startLogIndex != request.getPreLogIndex() + 1) {
                        //TODO
                        LOG.warn("flying.startLogIndex != request.getPreLogIndex() + 1");
                        continueSendEntries = false;
                        doUnlock = false;
                        replicator.sendEmptyEntries(false);
                        break;
                    }

                    response = (AppendEntriesResponse) rpcResponse.response;

                    LOG.info("curr term: {}, request seq {} [prev index: {}, prev term: {}, curr term: {}, entries size: {}]" +
                                    "\nresponse[term: {}, success?: {}, lastLogLast: {}]" +
                                    "\nflying[startLogIndex: {}]", this.options.getTerm(), seq,
                            request.getPreLogIndex(), request.getPrevLogTerm(), request.getTerm(), request.getEntriesCount(),
                            response.getTerm(), response.getSuccess(), response.getLastLogLast(),
                            flying.startLogIndex);


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
                        continueSendEntries = false;
                        doUnlock = false;
                        replicator.sendEmptyEntries(false);
                        break;
                    }

                    if (response.getTerm() != replicator.options.getTerm()) {
                        //TODO appendEntriesInFly clear
                        continueSendEntries = false;
                        break;
                    }

                    if (monotonicSendTimeMs > replicator.lastRpcSendTimestamp) {
                        replicator.lastRpcSendTimestamp = monotonicSendTimeMs;
                        LOG.info("update lastRpcSendTimestamp");
                    }
                    if (request.getEntriesCount() > 0) {
                        replicator.options.getBallotBox().commitAt(replicator.nextIndex,
                                replicator.nextIndex + request.getEntriesCount() - 1, replicator.options.getPeerId());
                    } else {
                        replicator.state = State.Replicate;
                    }
                    replicator.nextIndex += request.getEntriesCount();
                    LOG.warn("replicator.nextIndex: {}", replicator.nextIndex);
                    continueSendEntries = true;
                } finally {
                    //TODO
                    if (continueSendEntries) {
                        replicator.getAndIncrementRequiredNextSeq();
                    }
                }

            }

        } catch (Exception e) {
            LOG.error("", e);
        } finally {

            if (continueSendEntries) {
                // TODO send entries
                replicator.sendEntries();
            } else {
                if (doUnlock) {
                    lock.unlock();
                }
            }
        }
    }

    void resetInflights() {
        this.appendEntriesInFly.clear();
        this.pendingResponses.clear();
        final int rs = Math.max(this.reqSeq, this.requiredNextSeq);
        this.reqSeq = this.requiredNextSeq = rs;
    }

    private void addFlying(long startLogIndex, int entriesSize, int seq, CompletableFuture future) {
        this.fiying = new FlyingAppendEntries(startLogIndex, entriesSize, seq, future);
        this.appendEntriesInFly.add(fiying);
        LOG.warn(appendEntriesInFly.toString());
    }

    private long getNextSendIndex() {
        if (this.options.getPeerId().getPort() == 8890) {
            LOG.warn("appendEntriesInFly: {}, send index: {} ", this.appendEntriesInFly.size(), this.fiying);
        }
        if (this.appendEntriesInFly.isEmpty()) {
            return this.nextIndex;
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
        try {
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
        } finally {
            self.unlock();
        }
    }

    private void waitMoreEntries(final long nextWaitIndex) {
        LOG.warn("Node {} wait more entries, next index: {}", this.options.getPeerId(), nextWaitIndex);
        if (this.waitId > -1) {
            return;
        }
        this.waitId = this.options.getLogManager().wait(nextWaitIndex - 1,
                (objectlock, errorCode) -> continueSending(Replicator.this.self, errorCode), this.self);
    }

    static boolean continueSending(final ObjectLock<Replicator> lock, final int errCode) {
        Replicator replicator = lock.lock();
        replicator.LOG.info("Node {} continueSending next index: {}, appendEntriesInFly.size(): {}, requiredNextSeq: {}",
                replicator.options.getPeerId(), replicator.nextIndex, replicator.appendEntriesInFly.size(), replicator.requiredNextSeq);
        //TODO continueSending 未处理errCode，待完善
        replicator.waitId = -1;
        replicator.sendEntries();
        return true;
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
        LOG.info(request.toString());
        List<OutLogEntry> entries = new LinkedList<>();
        LOG.warn("prepareEntry start!!!");
        for (int i = 0; i < maxEntriesSize; i++) {
            if (!prepareEntry(nextSendingIndex, i, entries)) {
                LOG.info("prepareEntry end, nextSendingIndex: {}, i: {}, entries size: {}", nextSendingIndex, i, entries.size());
                break;
            }
        }
        if (entries.isEmpty()) {
            waitMoreEntries(nextSendingIndex);
            return false;
        }

        LOG.info("entries size: {}, first index: {}", entries.size(), entries.get(0).getId());
        request.setOutEntries(entries);
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

    private boolean prepareEntry(long nextSendIndex, int offset, List<OutLogEntry> entries) {
        long logIndex = nextSendIndex + offset;
        LogEntry entry = this.options.getLogManager().getEntry(logIndex);
        if (entry == null) {
            return false;
        }
        entries.add(OutLogEntry.getInstance(entry));
        LOG.info("entry log index: {}, entries size: {}", entry.getId(), entries.size());
        return true;
    }

    public static long getLastRpcSendTimestamp(final ObjectLock<Replicator> lock) {
        final Replicator r = lock.getData();
        if (r == null) {
            return 0L;
        }
        return r.lastRpcSendTimestamp;
    }

    public static void stop(ObjectLock<Replicator> self) {
        Replicator r = self.lock();
        try {
            for (final FlyingAppendEntries inflight : r.appendEntriesInFly) {
                if (inflight != r.fiying) {
                    if(inflight.future != null) {
                        inflight.future.cancel(true);
                    }
                }
            }
            r.heartbeatTimer.cancel(true);
            if (r.heartbeatInFly != null) {
                r.heartbeatInFly.cancel(true);
            }
        } finally {
            self.unlock();
        }

    }

}
