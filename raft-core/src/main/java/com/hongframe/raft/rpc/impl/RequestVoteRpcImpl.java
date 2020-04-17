package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    private static final Logger LOG = LoggerFactory.getLogger(RequestVoteRpcImpl.class);


    @Override
    public RequestVoteResponse preVote(RequestVoteRequest request) {
        NodeImpl node = getNode(request);
        return (RequestVoteResponse) node.handlePreVoteRequest(request);
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return (RequestVoteResponse) getNode(request).handleVoteRequest();
    }

}
