package com.hongframe.raft.rpc.core;

import static com.hongframe.raft.rpc.RpcRequests.*;

public interface RequestVoteRpc  {

    RequestVoteResponse preVote(RequestVoteRequest request);

    RequestVoteResponse requestVote(RequestVoteRequest request);

}
