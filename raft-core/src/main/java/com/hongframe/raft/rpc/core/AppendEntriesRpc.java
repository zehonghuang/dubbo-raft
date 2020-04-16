package com.hongframe.raft.rpc.core;

import static com.hongframe.raft.rpc.RpcRequests.*;

public interface AppendEntriesRpc extends RpcService {

    AppendEntriesResponse appendEntries(AppendEntriesRequest request);

}
