package com.hongframe.raft.rpc.core;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.ClientRequests.*;

public interface ClientRequestRpc extends RaftRpcService {

    Message getLeader(final GetLeaderRequest request);

}
