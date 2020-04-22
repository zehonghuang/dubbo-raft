package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.rpc.ClientRequests;
import com.hongframe.raft.rpc.core.ClientRequestRpc;

public class ClientRequestRpcImpl implements ClientRequestRpc {
    @Override
    public ClientRequests.GetLeaderResponse getLeader(ClientRequests.GetLeaderRequest request) {
        return null;
    }
}
