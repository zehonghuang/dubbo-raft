package com.hongframe.raft.rpc.core;

import com.hongframe.raft.rpc.ClientRequests.*;

public interface ClientRequestRpc extends RaftRpcService {

    GetLeaderResponse getLeader(final GetLeaderRequest request);

}
