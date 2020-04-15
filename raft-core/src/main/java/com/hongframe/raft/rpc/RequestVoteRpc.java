package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.RequestVoteRequest;
import com.hongframe.raft.entity.RequestVoteResponse;

public interface RequestVoteRpc {

    RequestVoteResponse preVote(RequestVoteRequest request);

}
