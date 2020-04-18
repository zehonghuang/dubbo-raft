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
import com.hongframe.raft.storage.impl.LogManagerImpl;
import com.hongframe.raft.util.ReentrantTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private NodeOptions nodeOptions;

    private RpcClient rpcClient;

    private ReentrantTimer voteTimer;
    private ReentrantTimer electionTimer;

    private LogManager logManager;


    public NodeImpl(String groupId, PeerId serverId) {
        this.groupId = groupId;
        this.serverId = serverId;
    }

    @Override
    public boolean init(NodeOptions opts) {

        this.logManager = new LogManagerImpl();
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

    public Message handlePreVoteRequest(final RequestVoteRequest voteRequest) {
        RequestVoteResponse voteResponse = new RequestVoteResponse();
        voteResponse.setPreVote(true);
        voteResponse.setTerm(voteRequest.getTerm());
        LOG.info(voteRequest.toString());
        return voteResponse;
    }

    public void handlePreVoteResponse(RequestVoteResponse voteResponse) {
        LOG.info(voteResponse.toString());
    }

    public Message handleVoteRequest() {
        return null;
    }


    public void handleVoteResponse(RequestVoteResponse voteResponse) {
        System.out.println(voteResponse);
    }

    private void handleElectionTimeout() {
        this.writeLock.lock();
        try {
            preVote();
        } finally {
            this.writeLock.unlock();
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
            NodeImpl.this.handlePreVoteResponse((RequestVoteResponse) getResponse());
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
