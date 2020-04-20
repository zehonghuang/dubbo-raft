package com.hongframe.raft.core;

import com.hongframe.raft.DubboRaftRpcFactory;
import com.hongframe.raft.Node;
import com.hongframe.raft.NodeManager;
import com.hongframe.raft.Status;
import com.hongframe.raft.conf.ConfigurationEntry;
import com.hongframe.raft.entity.*;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.rpc.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.RaftMetaStorage;
import com.hongframe.raft.storage.impl.LogManagerImpl;
import com.hongframe.raft.storage.impl.RaftMetaStorageImpl;
import com.hongframe.raft.util.ReentrantTimer;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
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
    private volatile long lastLeaderTimestamp;

    private NodeOptions nodeOptions;

    private RpcClient rpcClient;

    private ReentrantTimer voteTimer;
    private ReentrantTimer electionTimer;

    private LogManager logManager;
    private RaftMetaStorage metaStorage;


    public NodeImpl(String groupId, PeerId serverId) {
        this.groupId = groupId;
        this.serverId = serverId;
    }

    @Override
    public boolean init(NodeOptions opts) {

        this.logManager = new LogManagerImpl();
        this.metaStorage = new RaftMetaStorageImpl("." + File.separator + "raft_meta");
        this.currTerm = this.metaStorage.getTerm();
        this.voteId = this.metaStorage.getVotedFor().copy();

        this.nodeOptions = opts;
        this.conf = new ConfigurationEntry();
        this.conf.setConf(this.nodeOptions.getConfig());

        NodeManager.getInstance().add(this);

        this.rpcClient = DubboRaftRpcFactory.createRaftRpcClient();

        this.voteCtx.init(this.conf.getConf());
        this.prevoteCtx.init(this.conf.getConf());

        this.electionTimer = new ReentrantTimer("Dubbo-radt-ElectionTimer", this.nodeOptions.getElectionTimeoutMs()) {
            @Override
            protected void onTrigger() {
                handleElectionTimeout();
            }

            @Override
            protected int adjustTimeout(int timeoutMs) {
                return randomTimeout(timeoutMs);
            }
        };

        this.state = State.STATE_FOLLOWER;

        stepDown(this.currTerm, new Status());
        return true;
    }

    private int randomTimeout(final int timeoutMs) {
        return ThreadLocalRandom.current().nextInt(timeoutMs, timeoutMs + timeoutMs << 1);
    }

    public Message handlePreVoteRequest(final RequestVoteRequest request) {

        try {
            this.writeLock.lock();
            if (!this.state.isActive()) {
                return null;
            }
            final PeerId candidateId = new PeerId();
            if (!candidateId.parse(request.getServerId())) {
                return null;
            }
            boolean granted = false;

            do {
                if (this.leaderId != null && this.leaderId.isEmpty() && isCurrentLeaderValid()) {
                    break;
                }
                if (request.getTerm() < this.currTerm) {
                    //TODO 检查复制器
                    break;
                } else if (request.getTerm() == this.currTerm + 1) {
                    //TODO 检查复制器
                }
                this.writeLock.unlock();

                final LogId lastLogId = this.logManager.getLastLogId(true);

                this.writeLock.lock();

                final LogId requestLastLogId = new LogId(request.getTerm(), request.getLastLogIndex());

                granted = requestLastLogId.compareTo(lastLogId) >= 0;
            } while (false);//为了break出来

            RequestVoteResponse response = new RequestVoteResponse();
            response.setGranted(granted);
            response.setTerm(this.currTerm);
            response.setPreVote(true);
            return request;
        } finally {
            this.writeLock.unlock();
        }

    }

    public void handlePreVoteResponse(PeerId peerId, long term, RequestVoteResponse voteResponse) {
        this.writeLock.lock();
        try {
            if (this.state != State.STATE_FOLLOWER) {
                //不再是follower，不必要处理预选投票
                return;
            }
            if (this.currTerm != term) {
                //currTerm节点任期变了，无效
                return;
            }
            if (voteResponse.getTerm() > this.currTerm) {
                stepDown(voteResponse.getTerm(), null);
                return;
            }
            if (voteResponse.getGranted()) {
                this.prevoteCtx.grant(peerId);
                if (this.prevoteCtx.isGranted()) {
                    electSelf();
                }
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public Message handleRequestVoteRequest(final RequestVoteRequest request) {
        this.writeLock.lock();
        try {
            if (!this.state.isActive()) {
                return null;
            }
            final PeerId candidateId = new PeerId();
            if (candidateId.parse(request.getServerId())) {
                return null;
            }

            do {
                if (request.getTerm() >= this.currTerm) {
                    if (request.getTerm() > this.currTerm)  {
                        stepDown(request.getTerm(), new Status());
                    }
                } else {
                    break;
                }
                this.writeLock.unlock();

                final LogId lastLogId = this.logManager.getLastLogId(true);

                this.writeLock.lock();
                if(this.currTerm != request.getTerm()) {
                    break;
                }
                boolean isOk = new LogId(request.getTerm(), request.getLastLogIndex()).compareTo(lastLogId) >= 0;
                if (isOk && (this.voteId == null || this.voteId.isEmpty())) {
                    stepDown(this.currTerm, new Status());
                    this.voteId = candidateId.copy();
                    this.metaStorage.setTermAndVotedFor(this.currTerm, this.voteId);
                }

            } while (false);

            RequestVoteResponse response = new RequestVoteResponse();
            response.setGranted(request.getTerm() == this.currTerm && candidateId.equals(this.voteId));
            response.setPreVote(false);
            response.setTerm(this.currTerm);
            return response;

        } finally {
            this.writeLock.unlock();
        }
    }

    public void handleRequestVoteResponse(final long term, final PeerId peerId, RequestVoteResponse voteResponse) {

    }

    private void handleElectionTimeout() {
        this.writeLock.lock();
        try {
            preVote();
        } finally {
            this.writeLock.unlock();
        }
    }

    private void handleVoteTimeout() {

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
                LOG.warn(status.getErrorMsg());
            }
            NodeImpl.this.handlePreVoteResponse(peerId, term, (RequestVoteResponse) getResponse());
        }
    }

    private void preVote() {
        long oldTerm;
        this.writeLock.lock();
        try {
            oldTerm = this.currTerm;
        } finally {
            this.writeLock.unlock();
        }

        final LogId lastLogId = this.logManager.getLastLogId(true);//刷盘需要一段时间，所以释放锁，提高并发性

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

                this.rpcClient.requestVote(peerId, voteRequest, new PreVoteResponseCallback(this.currTerm, peerId, voteRequest));
            }

        } finally {
            this.writeLock.unlock();
        }

    }

    private void stepDown(final long term, final Status status) {
        if (!this.state.isActive()) {
            return;
        }

        this.leaderId = PeerId.emptyPeer();

        if (term > this.currTerm) {
            this.currTerm = term;
            this.voteId = PeerId.emptyPeer();
        }

        electionTimer.start();
    }

    private void electSelf() {
        this.writeLock.lock();
        long oldTerm;
        try {
            if (this.conf.contains(this.serverId)) {
                //该节点被移除
                return;
            }
            if (this.state == State.STATE_FOLLOWER) {
                this.electionTimer.stop();
            }
            this.leaderId = PeerId.emptyPeer();
            this.state = State.STATE_CANDIDATE;
            this.currTerm++;
            this.voteId = this.serverId.copy();
            //TODO vote timer 未实现
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
                this.rpcClient.requestVote(peerId, request, callback);
            }
            this.metaStorage.setTermAndVotedFor(this.currTerm, this.serverId);
            this.voteCtx.grant(this.serverId);
            if (this.voteCtx.isGranted()) {
                //TODO becomeLeader();
            }

        } finally {
            this.writeLock.unlock();
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
    public void shutdown() {

    }

}
