package com.hongframe.raft.rpc;

import static com.hongframe.raft.rpc.RpcRequests.*;

public interface RequestVoteRpc {

    RequestVoteResponse preVote(RequestVoteRequest request);

    RequestVoteResponse requestVote(RequestVoteRequest request);

}
