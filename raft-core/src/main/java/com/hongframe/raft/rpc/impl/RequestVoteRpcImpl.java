package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Node;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.rpc.core.RequestVoteRpc;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    private NodeImpl node;

    @Override
    public RequestVoteResponse preVote(RequestVoteRequest request) {
        return (RequestVoteResponse) this.node.handlePreVoteRequest(request);
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return (RequestVoteResponse) this.node.handleVoteRequest();
    }

    @Override
    public void setNode(Node node) {
        this.node = (NodeImpl) node;
    }
}
