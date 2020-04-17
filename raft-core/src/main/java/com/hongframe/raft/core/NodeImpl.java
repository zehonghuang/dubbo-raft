package com.hongframe.raft.core;

import com.hongframe.raft.DubboRaftRpcFactory;
import com.hongframe.raft.Node;
import com.hongframe.raft.NodeManager;
import com.hongframe.raft.entity.Ballot;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.rpc.RpcClient;
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

    private State state;
    private Ballot voteCtx = new Ballot();
    private Ballot prevoteCtx = new Ballot();

    private String groupId;
    private PeerId serverId;
    private PeerId leaderId;
    private NodeId nodeId;


    private NodeOptions nodeOptions;

    private RpcClient rpcClient;

    private ReentrantTimer voteTimer;
    private ReentrantTimer electionTimer;


    public NodeImpl(String groupId, PeerId serverId) {
        this.groupId = groupId;
        this.serverId = serverId;
    }

    @Override
    public boolean init(NodeOptions opts) {
        this.nodeOptions = opts;

        NodeManager.getInstance().add(this);

        this.rpcClient = DubboRaftRpcFactory.createRaftRpcClient();

        this.voteCtx.init(this.nodeOptions.getConfig());
        this.prevoteCtx.init(this.nodeOptions.getConfig());

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

        return true;
    }

    private int randomTimeout(final int timeoutMs) {
        return ThreadLocalRandom.current().nextInt(timeoutMs, timeoutMs + timeoutMs >> 1);
    }

    public Message handlePreVoteRequest(RequestVoteRequest voteRequest) {
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
