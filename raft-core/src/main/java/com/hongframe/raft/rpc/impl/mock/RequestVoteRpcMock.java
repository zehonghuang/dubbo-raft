package com.hongframe.raft.rpc.impl.mock;

import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.RequestVoteRpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-18 12:44
 */
public class RequestVoteRpcMock implements RequestVoteRpc {
    @Override
    public Response<RequestVoteResponse> preVote(RequestVoteRequest request) {
        return new Response<>(new ErrorResponse());
    }

    @Override
    public Response<RequestVoteResponse> requestVote(RequestVoteRequest request) {
        return null;
    }
}
