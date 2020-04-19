package com.hongframe.raft.rpc.core;

import com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface MembershipChangeRpc extends RpcService {

    Response<AddPeerResponse> addPeer(AddPeerRequest request);

    Response<RemovePeerResponse> removePeer(RemovePeerRequest request);

    Response<ChangePeersResponse> changePeer(ChangePeersRequest request);

    void resetPeer();

    void transferLeadershipTo();

}
