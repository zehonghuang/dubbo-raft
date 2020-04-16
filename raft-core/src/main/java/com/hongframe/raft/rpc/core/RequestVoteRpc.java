package com.hongframe.raft.rpc.core;

import static com.hongframe.raft.rpc.RpcRequests.*;

public interface RequestVoteRpc extends RpcService  {

    RequestVoteResponse preVote(RequestVoteRequest request);

    RequestVoteResponse requestVote(RequestVoteRequest request);

}
