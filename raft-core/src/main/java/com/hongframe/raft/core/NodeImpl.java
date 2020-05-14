package com.hongframe.raft.core;

import com.hongframe.raft.*;
import com.hongframe.raft.callback.*;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.conf.ConfigurationEntry;
import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.*;
import com.hongframe.raft.entity.codec.proto.ProtoLogEntryCodecFactory;
import com.hongframe.raft.option.*;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.storage.RaftMetaStorage;
import com.hongframe.raft.storage.impl.LogManagerImpl;
import com.hongframe.raft.storage.impl.RaftMetaStorageImpl;
import com.hongframe.raft.storage.impl.RocksDBLogStorage;
import com.hongframe.raft.util.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 01:40
 */
public class NodeImpl implements Node {

    private static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private volatile State state;
    private long currTerm;
    private Ballot voteCtx = new Ballot();
    private Ballot prevoteCtx = new Ballot();

    private String groupId;
    private PeerId serverId;
    private PeerId leaderId;
    private PeerId voteId;
    private NodeId nodeId;
    private ConfigurationEntry conf;
    private ConfigurationManager configurationManager;
    private volatile long lastLeaderTimestamp;

    private NodeOptions nodeOptions;
    private RaftOptions raftOptions;

    private RpcClient rpcClient;

    private ReentrantTimer voteTimer;
    private ReentrantTimer electionTimer;
    private ReentrantTimer stepDownTimer;
    private Scheduler timerManger;
    private Disruptor<LogEntrAndCallback> applyDisruptor;
    private RingBuffer<LogEntrAndCallback> applyQueue;

    private CallbackQueue callbackQueue;
    private BallotBox ballotBox;
    private LogStorage logStorage;
    private LogManager logManager;
    private FSMCaller caller;
    private RaftMetaStorage metaStorage;
    private ReplicatorGroup replicatorGroup;


    public NodeImpl(String groupId, PeerId serverId) {
        this.groupId = groupId;
        this.serverId = serverId;
        this.nodeId = new NodeId(this.groupId, this.serverId);
    }

    private static class LogEntrAndCallback {
        Callback callback;
        LogEntry entry;
    }

    private class LogEntryCallbackFactory implements EventFactory<LogEntrAndCallback> {
        @Override
        public LogEntrAndCallback newInstance() {
            return new LogEntrAndCallback();
        }
    }

    private class LogEntryCallbackEventHandler implements EventHandler<LogEntrAndCallback> {

        private final List<LogEntrAndCallback> tasks = new ArrayList<>();

        @Override
        public void onEvent(LogEntrAndCallback event, long sequence, boolean endOfBatch) throws Exception {
            LOG.info("apply -> LogEntryCallbackEventHandler.onEvent");
            tasks.add(event);
            if (tasks.size() >= NodeImpl.this.raftOptions.getApplyBatch() || endOfBatch) {
                executeTasks(tasks);
                this.tasks.clear();
            }
        }
    }

    @Override
    public boolean init(NodeOptions opts) {
        NodeManager.getInstance().add(this);

        this.nodeOptions = opts;
        this.raftOptions = this.nodeOptions.getRaftOptions();

        this.configurationManager = new ConfigurationManager();
        this.conf = new ConfigurationEntry();
        this.conf.setConf(this.nodeOptions.getConfig());

        this.voteCtx.init(this.conf.getConf());
        this.prevoteCtx.init(this.conf.getConf());

        this.timerManger = new TimerManager(Utils.CPUS);
        this.electionTimer = new ReentrantTimer("Dubbo-raft-ElectionTimer", this.nodeOptions.getElectionTimeoutMs()) {
            @Override
            protected void onTrigger() {
                handleElectionTimeout();
            }

            @Override
            protected int adjustTimeout(int timeoutMs) {
                return randomTimeout(timeoutMs);
            }
        };
        this.voteTimer = new ReentrantTimer("Dubbo-raft-VoteTimer", this.nodeOptions.getElectionTimeoutMs()) {
            @Override
            protected void onTrigger() {
                handleVoteTimeout();
            }

            @Override
            protected int adjustTimeout(int timeoutMs) {
                return randomTimeout(timeoutMs);
            }
        };
        this.stepDownTimer = new ReentrantTimer("Dubbo-raft-StepDownTimer", this.nodeOptions.getElectionTimeoutMs() >> 1) {
            @Override
            protected void onTrigger() {
                handleStepDownTimeout();
            }
        };

        this.logStorage = new RocksDBLogStorage(this.nodeOptions.getLogUri() + File.separator + "raft_data" + File.separator + this.nodeId.getPeerId().getPort());
        LogStorageOptions logStorageOptions = new LogStorageOptions();
        logStorageOptions.setConfigurationManager(this.configurationManager);
        logStorageOptions.setCodecFactory(new ProtoLogEntryCodecFactory());//TODO setCodecFactory
        this.logStorage.init(logStorageOptions);

        this.logManager = new LogManagerImpl();
        LogManagerOptions logManagerOptions = new LogManagerOptions();
        logManagerOptions.setLogStorage(this.logStorage);
        logManagerOptions.setRaftOptions(this.raftOptions);
        logManagerOptions.setCaller(this.caller);
        logManagerOptions.setConfigurationManager(this.configurationManager);
        this.logManager.init(logManagerOptions);

        this.metaStorage = new RaftMetaStorageImpl(this.nodeOptions.getLogUri() + File.separator + "raft_meta" + File.separator + this.serverId.toString());
        this.currTerm = this.metaStorage.getTerm();
        this.voteId = this.metaStorage.getVotedFor().copy();

        this.callbackQueue = new CallbackQueueImpl();
        FSMCallerOptions fsmCallerOptions = new FSMCallerOptions();
        fsmCallerOptions.setFsm(this.nodeOptions.getStateMachine());
        fsmCallerOptions.setBootstrapId(new LogId());
        fsmCallerOptions.setNode(this);
        fsmCallerOptions.setCallbackQueue(this.callbackQueue);
        fsmCallerOptions.setLogManager(this.logManager);
        this.caller = new FSMCallerImpl();
        this.caller.init(fsmCallerOptions);

        BallotBoxOptions ballotBoxOptions = new BallotBoxOptions();
        ballotBoxOptions.setCaller(this.caller);
        ballotBoxOptions.setCallbackQueue(this.callbackQueue);
        this.ballotBox = new BallotBox();
        this.ballotBox.init(ballotBoxOptions);

        this.applyDisruptor = DisruptorBuilder.<LogEntrAndCallback>newInstance() //
                .setRingBufferSize(this.raftOptions.getDisruptorBufferSize()) //
                .setEventFactory(new LogEntryCallbackFactory()) //
                .setThreadFactory(new NamedThreadFactory("Dubbo-Raft-NodeImpl-Disruptor-", true)) //
                .setProducerType(ProducerType.MULTI) //
                .setWaitStrategy(new BlockingWaitStrategy()) //
                .build();
        this.applyDisruptor.handleEventsWith(new LogEntryCallbackEventHandler());
        this.applyDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.applyQueue = this.applyDisruptor.start();

        this.rpcClient = DubboRaftRpcFactory.createRaftRpcClient();
        this.replicatorGroup = new ReplicatorGroupImpl();
        ReplicatorGroupOptions rgo = new ReplicatorGroupOptions();
        rgo.setElectionTimeoutMs(this.nodeOptions.getElectionTimeoutMs());
        rgo.setHeartbeatTimeoutMs(heartbeatTimeout(this.nodeOptions.getElectionTimeoutMs()));
        rgo.setLogManager(this.logManager);
        rgo.setNode(this);
        rgo.setRpcClient(this.rpcClient);
        rgo.setTimerManager(this.timerManger);
        rgo.setBallotBox(this.ballotBox);
        rgo.setRaftOptions(this.raftOptions);
        this.replicatorGroup.init(this.nodeId.copy(), rgo);

        this.state = State.STATE_FOLLOWER;

        stepDown(this.currTerm, new Status(10001, "node init"));
        return true;
    }

    private int randomTimeout(final int timeoutMs) {
        return ThreadLocalRandom.current().nextInt(timeoutMs, timeoutMs +  + this.raftOptions.getMaxElectionDelayMs());
    }

    private int heartbeatTimeout(final int electionTimeout) {
        return Math.max(electionTimeout / this.raftOptions.getElectionHeartbeatFactor(), 10);
    }

    public Message handlePreVoteRequest(final RequestVoteRequest request) {
        boolean doUnlock = true;
        LOG.warn("form peerId: {}", request.getServerId());
        this.writeLock.lock();
        LOG.info("from {} pre vote request, term: {}", request.getServerId(), request.getTerm());
        try {

            if (!this.state.isActive()) {
                return null;
            }
            final PeerId candidateId = new PeerId();
            if (!candidateId.parse(request.getServerId())) {
                return null;
            }
            boolean granted = false;

            do {
                if (this.leaderId != null && !this.leaderId.isEmpty() && isCurrentLeaderValid()) {
                    break;
                }
                if (request.getTerm() < this.currTerm) {
                    //TODO 检查复制器
                    break;
                } else if (request.getTerm() == this.currTerm + 1) {
                    //TODO 检查复制器
                }
                doUnlock = false;
                this.writeLock.unlock();

                final LogId lastLogId = this.logManager.getLastLogId(true);
                LOG.info("last log id: {}", lastLogId);

                doUnlock = true;
                this.writeLock.lock();

                final LogId requestLastLogId = new LogId(request.getTerm(), request.getLastLogIndex());

                granted = requestLastLogId.compareTo(lastLogId) >= 0;
            } while (false);//为了break出来

            RequestVoteResponse response = new RequestVoteResponse();
            response.setGranted(granted);
            response.setTerm(this.currTerm);
            response.setPreVote(true);
            LOG.info("are you grant for {} ? {}", request.getServerId(), granted);
            return response;
        } finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }

    }

    public void handlePreVoteResponse(PeerId peerId, long term, RequestVoteResponse voteResponse) {
        LOG.info("peer {} grant to you? {}", peerId, voteResponse.getGranted());
        boolean doUnlock = true;
        this.writeLock.lock();
        try {
            if (this.state != State.STATE_FOLLOWER) {
                //不再是follower，不必要处理预选投票
                LOG.warn("state chanege to {}", this.state);
                return;
            }
            if (this.currTerm != term) {
                //currTerm节点任期变了，无效
                LOG.warn("cuurTerm: {}", this.currTerm);
                return;
            }
            if (voteResponse.getTerm() > this.currTerm) {
                stepDown(voteResponse.getTerm(), new Status(10001, "voteResponse.getTerm() > this.currTerm"));
                return;
            }
            if (voteResponse.getGranted()) {
                this.prevoteCtx.grant(peerId);
                if (this.prevoteCtx.isGranted()) {
                    LOG.info("peer {} granted pre vote >>> electSelf()", this.serverId);
                    doUnlock = false;
                    electSelf();
                }
            }
        } finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }
    }

    public Message handleRequestVoteRequest(final RequestVoteRequest request) {
        boolean doUnlock = true;
        this.writeLock.lock();
        try {
            if (!this.state.isActive()) {
                return null;
            }
            final PeerId candidateId = new PeerId();
            if (!candidateId.parse(request.getServerId())) {
                return null;
            }

            do {
                if (request.getTerm() >= this.currTerm) {
                    if (request.getTerm() > this.currTerm) {
                        LOG.info(request.toString());
                        stepDown(request.getTerm(), new Status(10001, "requset vote request: .getTerm() > this.currTerm"));
                    }
                } else {
                    break;
                }
                doUnlock = false;
                this.writeLock.unlock();

                final LogId lastLogId = this.logManager.getLastLogId(true);

                doUnlock = true;
                this.writeLock.lock();
                if (this.currTerm != request.getTerm()) {
                    LOG.warn("this.currTerm != request.getTerm()");
                    break;
                }
                boolean isOk = new LogId(request.getTerm(), request.getLastLogIndex()).compareTo(lastLogId) >= 0;
                if (isOk && (this.voteId == null || this.voteId.isEmpty())) {
                    stepDown(request.getTerm(), new Status(10001, "isOk && (this.voteId == null || this.voteId.isEmpty())"));
                    this.voteId = candidateId.copy();
                    this.metaStorage.setTermAndVotedFor(this.currTerm, this.voteId);
                }
                LOG.info("candidateId: {}, voteId: {}", candidateId, this.voteId);
            } while (false);

            RequestVoteResponse response = new RequestVoteResponse();
            response.setGranted(request.getTerm() == this.currTerm && candidateId.equals(this.voteId));
            response.setPreVote(false);
            response.setTerm(this.currTerm);
            LOG.info(response.toString());
            return response;

        } finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }
    }

    public void handleRequestVoteResponse(final long term, final PeerId peerId, RequestVoteResponse response) {
        this.writeLock.lock();

        try {
            if (this.state != State.STATE_CANDIDATE) {
                return;
            }
            if (this.currTerm != term) {
                return;
            }
            LOG.info("peer id {}, {}", peerId, response.toString());
            if (response.getTerm() > this.currTerm) {
                stepDown(response.getTerm(), new Status(10001, "request vote response: .getTerm() > this.currTerm"));
            }
            if (response.getGranted()) {
                this.voteCtx.grant(peerId);
                if (this.voteCtx.isGranted()) {
                    becomeLeader();
                }
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    private class FollowerFlushDoneCallback extends LogManager.FlushDoneCallback {

        private final RequestCallback callback;
        private final long term;
        private final long committedIndex;
        private final NodeImpl node;

        public FollowerFlushDoneCallback(List<LogEntry> entries, RequestCallback callback, AppendEntriesRequest request, long term, NodeImpl node) {
            super(entries);
            this.callback = callback;
            this.term = term;
            this.node = node;
            this.committedIndex = Math.min(request.getCommittedIndex(), request.getPreLogIndex() + request.getEntriesCount());
        }

        @Override
        public void run(Status status) {
            if (!status.isOk()) {
                this.callback.run(status);
                return;
            }

            AppendEntriesResponse response = new AppendEntriesResponse();
            this.node.readLock.lock();

            try {
                if (this.term != this.node.currTerm) {
                    response.setSuccess(false);
                    response.setTerm(this.node.currTerm);
                    callback.sendResponse(response);
                    return;
                }
            } finally {
                this.node.readLock.unlock();
            }

            response.setSuccess(true);
            response.setTerm(this.node.currTerm);
            this.node.ballotBox.setLastCommittedIndex(this.committedIndex);
            callback.sendResponse(response);
        }
    }

    public Message handleAppendEntriesRequest(final AppendEntriesRequest request, RequestCallback callback) {
        boolean doUnlock = true;
        this.writeLock.lock();
        final int entriesCount = request.getEntriesCount();
        try {
            if (!this.state.isActive()) {
                return new ErrorResponse(10001, "node not active");
            }
            PeerId peerId = new PeerId();
            if (!peerId.parse(request.getServerId())) {
                return new ErrorResponse(10001, "server parse fail");
            }
            if (request.getTerm() < this.currTerm) {
                AppendEntriesResponse response = new AppendEntriesResponse();
                response.setSuccess(false);
                response.setTerm(this.currTerm);
                return response;
            }
            if (this.leaderId == null || this.leaderId.isEmpty()) {
                if(this.currTerm != request.getTerm()) {
                    this.currTerm = request.getTerm();
                }
                this.leaderId = peerId;
                LOG.info("this leader id: {}", this.leaderId);
            }

            updateLastLeaderTimestamp(Utils.monotonicMs());

            long reqPrevIndex = request.getPreLogIndex();
            long reqPrevTerm = request.getPrevLogTerm();
            long localPervTerm = this.logManager.getTerm(reqPrevIndex);
            if (reqPrevTerm != localPervTerm) {
                AppendEntriesResponse response = new AppendEntriesResponse();
                response.setSuccess(false);
                response.setTerm(this.currTerm);
                response.setLastLogLast(this.logManager.getLastLogIndex());
                return response;
            }
            if (entriesCount == 0) {
                AppendEntriesResponse response = new AppendEntriesResponse();
                response.setSuccess(true);
                response.setTerm(this.currTerm);
                response.setLastLogLast(this.logManager.getLastLogIndex());
                doUnlock = false;
                this.writeLock.unlock();
                if(this.nodeId.getPeerId().getPort() == 8890) {
                    LOG.warn("setLastCommittedIndex: {}, reqPrevIndex: {}",request.getCommittedIndex(), reqPrevIndex);
                }
                this.ballotBox.setLastCommittedIndex(Math.min(request.getCommittedIndex(), reqPrevIndex));
                return response;
            }


            List<LogEntry> entries = new ArrayList<>(entriesCount);
            List<OutLogEntry> requestEntries = request.getOutEntries();

            for (int i = 0; i < entriesCount; i++) {
                //TODO check sum
                entries.add(LogEntry.getInstance(requestEntries.get(i)));
            }
            LOG.info("request body: {}", request.toString());
            this.logManager.appendEntries(entries, new FollowerFlushDoneCallback(entries, callback, request, this.currTerm, this));
        } catch (Exception e) {
            LOG.error("", e);
        }finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }
        return null;
    }

    private void updateLastLeaderTimestamp(final long lastLeaderTimestamp) {
        this.lastLeaderTimestamp = lastLeaderTimestamp;
    }

    private void handleElectionTimeout() {
        if(!this.leaderId.isEmpty()) {
            LOG.info("peer {} election time out, lastCommittedIndex: {}, begin pre Vote, my leader is {}", this.serverId, this.ballotBox.getLastCommittedIndex(), this.leaderId);
        } else {
            LOG.info("leeader is empty!!!");
        }
        boolean doUnlock = true;
        this.writeLock.lock();
        try {
            if (this.state != State.STATE_FOLLOWER) {
                return;
            }
            if (isCurrentLeaderValid()) {
                return;
            }
            doUnlock = false;
            preVote();
        } finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }
    }

    private void handleVoteTimeout() {
        //TODO handleVoteTimeout
    }

    private final class RequestVoteResponseCallback extends ResponseCallbackAdapter {

        final long term;
        final PeerId peerId;
        final RequestVoteRequest request;

        public RequestVoteResponseCallback(long term, PeerId peerId, RequestVoteRequest request) {
            this.term = term;
            this.peerId = peerId;
            this.request = request;
        }

        @Override
        public void run(Status status) {
            if (!status.isOk()) {
                LOG.warn(status.getErrorMsg());
                return;
            }
            NodeImpl.this.handleRequestVoteResponse(this.term, this.peerId, (RequestVoteResponse) getResponse());
        }
    }

    private final class PreVoteResponseCallback extends ResponseCallbackAdapter {

        final long term;
        final PeerId peerId;
        final RequestVoteRequest request;

        public PreVoteResponseCallback(long term, PeerId peerId, RequestVoteRequest request) {
            this.term = term;
            this.peerId = peerId;
            this.request = request;
        }

        @Override
        public void run(Status status) {
            if (!status.isOk()) {
                LOG.warn("PreVoteResponseCallback[{}]", status.getErrorMsg());
                return;
            }
            NodeImpl.this.handlePreVoteResponse(peerId, term, (RequestVoteResponse) getResponse());
        }
    }

    private void preVote() {
        long oldTerm;
        try {
            oldTerm = this.currTerm;
        } finally {
            this.writeLock.unlock();
        }

        final LogId lastLogId = this.logManager.getLastLogId(true);//刷盘需要一段时间，所以释放锁，提高并发性

        Boolean doUnlock = true;
        this.writeLock.lock();
        try {
            if (this.currTerm != oldTerm) {
                return;
            }
            this.prevoteCtx.init(this.conf.getConf());
            for (PeerId peerId : this.conf.getConf().getPeers()) {
                if (peerId.equals(this.serverId)) {
                    continue;
                }
                if (!rpcClient.connect(peerId)) {
                    LOG.error("fail connection peer: {}", peerId);
                }
                RequestVoteRequest voteRequest = new RequestVoteRequest();
                voteRequest.setPreVote(true);
                voteRequest.setGroupId(this.groupId);
                voteRequest.setPeerId(peerId.toString());
                voteRequest.setServerId(this.serverId.toString());
                voteRequest.setTerm(this.currTerm + 1);
                voteRequest.setLastLogIndex(lastLogId.getIndex());
                voteRequest.setLastLogTerm(lastLogId.getTerm());
                LOG.info("request pre vote to {}, {}", peerId, voteRequest);
                this.rpcClient.requestVote(peerId, voteRequest, new PreVoteResponseCallback(this.currTerm, peerId, voteRequest));
            }

            this.prevoteCtx.grant(this.serverId);
            if (this.prevoteCtx.isGranted()) {
                doUnlock = false;
                electSelf();
            }
        } finally {
            if (doUnlock) {
                this.writeLock.unlock();
            }
        }

    }

    private void becomeLeader() {
        try {
            this.state = State.STATE_LEADER;
            this.leaderId = this.serverId.copy();
            this.voteTimer.stop();
            this.replicatorGroup.resetTerm(this.currTerm);
            LOG.info("peer {} become Leader", this.leaderId);
            this.replicatorGroup.resetTerm(this.currTerm);
            this.ballotBox.resetPendingIndex(logManager.getLastLogIndex() + 1);
            for (PeerId peerId : this.conf.getConf().getPeers()) {
                if (peerId.equals(this.serverId)) {
                    continue;
                }
                this.replicatorGroup.addReplicator(peerId);
            }
            LOG.info("init replicatorGroup end");

            //TODO 这是要去掉的，非法操作
            this.nodeOptions.getStateMachine().onLeaderStart(this.currTerm);
        } catch (Exception e) {
            LOG.error("", e);
        }

        this.stepDownTimer.start();
    }

    private void handleStepDownTimeout() {
        this.writeLock.lock();
        try {
            long monotonicNowMs = Utils.monotonicMs();
            checkDeadNodes(this.conf.getConf(), monotonicNowMs);
        } finally {
            this.writeLock.unlock();
        }
    }

    private void checkDeadNodes(final Configuration conf, final long monotonicNowMs) {
        List<PeerId> peerIds = conf.getPeers();
        if (checkDeadNodes0(peerIds, monotonicNowMs)) {
            return;
        }
        stepDown(this.currTerm, new Status(10001, "checkDeadNodes alive"));

    }

    private boolean checkDeadNodes0(final List<PeerId> peers, final long monotonicNowMs) {
        final int leaderLeaseTimeoutMs = this.nodeOptions.getLeaderLeaseTimeoutMs();
        int aliveCount = 0;
        long startLease = Long.MAX_VALUE;
        for (PeerId peerId : peers) {
            if (peerId.equals(this.serverId)) {
                aliveCount++;
                continue;
            }
            final long lastRpcSendTimestamp = this.replicatorGroup.getLastRpcSendTimestamp(peerId);
            if (monotonicNowMs - lastRpcSendTimestamp <= leaderLeaseTimeoutMs) {
                aliveCount++;
                if (startLease > lastRpcSendTimestamp) {
                    startLease = lastRpcSendTimestamp;
                }
                continue;
            }
        }
        if (aliveCount >= peers.size() / 2 + 1) {
            updateLastLeaderTimestamp(startLease);
            return true;
        }

        return false;
    }

    private void stepDown(final long term, final Status status) {
        if (!this.state.isActive()) {
            return;
        }
        LOG.error("status : {}", status.getErrorMsg());
        this.stepDownTimer.stop();

        this.leaderId = PeerId.emptyPeer();
        LOG.info("leader id change empty!!!");
        if (term > this.currTerm) {
            this.currTerm = term;
            this.voteId = PeerId.emptyPeer();
        }

        this.replicatorGroup.stopAll();

        electionTimer.start();
    }

    private void electSelf() {
        LOG.info("into electSelf() {}", this.state);
        long oldTerm;
        try {
            if (!this.conf.contains(this.serverId)) {
                //该节点被移除
                return;
            }
            if (this.state == State.STATE_FOLLOWER) {
                LOG.info("stop electionTimer");
                this.electionTimer.stop();
            }
            this.leaderId = PeerId.emptyPeer();
            LOG.info("leader id change empty!!!");
            this.state = State.STATE_CANDIDATE;
            this.currTerm++;
            this.voteId = this.serverId.copy();
            this.voteTimer.start();
            this.voteCtx.init(this.conf.getConf());
            oldTerm = this.currTerm;
        } finally {
            this.writeLock.unlock();
        }

        final LogId lastLogId = this.logManager.getLastLogId(true);

        this.writeLock.lock();
        try {
            if (this.currTerm != oldTerm) {
                return;
            }
            for (PeerId peerId : this.conf.getConf().getPeers()) {
                if (peerId.equals(this.serverId)) {
                    continue;
                }

                if (!this.rpcClient.connect(peerId)) {
                    continue;
                }
                RequestVoteRequest request = new RequestVoteRequest();
                request.setGroupId(this.groupId);
                request.setPeerId(peerId.toString());
                request.setServerId(this.serverId.toString());
                request.setTerm(this.currTerm);
                request.setLastLogTerm(lastLogId.getTerm());
                request.setLastLogIndex(lastLogId.getIndex());

                RequestVoteResponseCallback callback = new RequestVoteResponseCallback(this.currTerm, peerId, request);
                LOG.info(request.toString());
                this.rpcClient.requestVote(peerId, request, callback);
            }
            this.metaStorage.setTermAndVotedFor(this.currTerm, this.serverId);
            this.voteCtx.grant(this.serverId);
            if (this.voteCtx.isGranted()) {
                becomeLeader();
            }

        } finally {
            this.writeLock.unlock();
            LOG.warn("electSelf end");
        }

    }

    private boolean isCurrentLeaderValid() {
        return Utils.monotonicMs() - this.lastLeaderTimestamp < this.nodeOptions.getElectionTimeoutMs();
    }

    @Override
    public NodeId getNodeId() {
        if (this.nodeId == null) {
            this.nodeId = new NodeId(this.groupId, this.serverId);
        }
        return this.nodeId;
    }

    @Override
    public PeerId getLeaderId() {
        return this.leaderId;
    }

    @Override
    public void apply(Task task) {

        LOG.info("into apply");
        LogEntry entry = new LogEntry();
        entry.setData(task.getData());

        final EventTranslator<LogEntrAndCallback> translator = (event, seq) -> {
            event.callback = task.getCallback();
            event.entry = entry;
            LOG.info("Task -> LogEntrAndCallback");
        };
        while (true) {
            if (this.applyQueue.tryPublishEvent(translator)) {
                break;
            }
        }
    }

    private class LeaderFlushDoneCallback extends LogManager.FlushDoneCallback {
        public LeaderFlushDoneCallback(List<LogEntry> entries) {
            super(entries);
        }

        @Override
        public void run(Status status) {
            LOG.info("executeTasks -> LeaderFlushDoneCallback: {}", this.nEntries);
            if (status.isOk()) {
                NodeImpl.this.ballotBox.commitAt(this.firstLogIndex, this.firstLogIndex + this.nEntries - 1,
                        NodeImpl.this.serverId);
            }
        }
    }

    private void executeTasks(final List<LogEntrAndCallback> tasks) {
        LOG.info("onEvent -> executeTasks");
        this.writeLock.lock();
        try {
            if (this.state != State.STATE_LEADER) {
                Status status = new Status(10001, "");
                Utils.runInThread(() -> {
                    for (LogEntrAndCallback callback : tasks) {
                        callback.callback.run(status);
                    }
                });
                return;
            }
            List<LogEntry> entries = new ArrayList<>(tasks.size());
            for (LogEntrAndCallback task : tasks) {
                //TODO executeTasks
                if (!this.ballotBox.appendPendingTask(this.conf.getConf(), null, task.callback)) {
                    continue;
                }
                task.entry.getId().setTerm(this.currTerm);
                task.entry.setType(EntryType.ENTRY_TYPE_DATA);
                entries.add(task.entry);
            }
            LOG.info("how much entries: {}", entries.size());
            this.logManager.appendEntries(entries, new LeaderFlushDoneCallback(entries));
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void shutdown() {

    }

}
