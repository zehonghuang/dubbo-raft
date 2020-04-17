package com.hongframe.raft.core;

import com.hongframe.raft.DubboRaftRpcFactory;
import com.hongframe.raft.Node;
import com.hongframe.raft.NodeManager;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.ReentrantTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 01:40
 */
public class NodeImpl implements Node {

    private static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);

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

        PeerId t = null;
        for(PeerId peerId : this.nodeOptions.getConfig().getPeers()) {
            if(peerId.equals(this.serverId)) {
                continue;
            }
            t = peerId;
            rpcClient.connect(peerId);
        }
        final PeerId p = t;
        this.electionTimer = new ReentrantTimer("Dubbo-radt-ElectionTimer", this.nodeOptions.getElectionTimeoutMs()) {
            @Override
            protected void onTrigger() {
                LOG.info("runing electionTimer");
                RpcRequests.RequestVoteRequest voteRequest = new RpcRequests.RequestVoteRequest();
                voteRequest.setGroupId("raft");
                voteRequest.setTerm(100L);
                voteRequest.setPeerId(p.toString());
                voteRequest.setPreVote(true);

                rpcClient.requestVote(p, voteRequest, message -> {
                    LOG.info("peer: {}", p);
                    handlePreVoteResponse((RequestVoteResponse) message);
                });
            }
        };
        electionTimer.start();
        return false;
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
