package com.hongframe.raft.core;

import com.hongframe.raft.Node;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RaftOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 01:40
 */
public class NodeImpl implements Node {

    private static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);

    private PeerId serverId;
    private PeerId leaderId;

    @Override
    public boolean init(RaftOptions opts) {
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
    public void shutdown() {

    }

}