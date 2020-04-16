package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.rpc.core.RequestVoteRpc;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {


    @Override
    public RequestVoteResponse preVote(RequestVoteRequest request) {

        return (RequestVoteResponse) getNode(request).handlePreVoteRequest(request);
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return (RequestVoteResponse) getNode(request).handleVoteRequest();
    }

}
