package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.MembershipChangeRpc;

public class MembershipChangeRpcImpl implements MembershipChangeRpc {
    @Override
    public Response<AddPeerResponse> addPeer(AddPeerRequest request) {
        return null;
    }

    @Override
    public Response<RemovePeerResponse> removePeer(RemovePeerRequest request) {
        return null;
    }

    @Override
    public Response<ChangePeersResponse> changePeer(ChangePeersRequest request) {
        return null;
    }

    @Override
    public void resetPeer() {

    }

    @Override
    public void transferLeadershipTo() {

    }
}
