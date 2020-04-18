package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    private static final Logger LOG = LoggerFactory.getLogger(RequestVoteRpcImpl.class);


    @Override
    public Response<RequestVoteResponse> preVote(RequestVoteRequest request) {
        Message message = getNode(request).handlePreVoteRequest(request);
        return checkResponse(message);
    }

    @Override
    public Response<RequestVoteResponse> requestVote(RequestVoteRequest request) {
        Message message = getNode(request).handleVoteRequest();
        return checkResponse(message);
    }

}
