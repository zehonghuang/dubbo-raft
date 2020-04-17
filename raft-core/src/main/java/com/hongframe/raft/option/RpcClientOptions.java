package com.hongframe.raft.option;

import com.hongframe.raft.entity.PeerId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 22:50
 */
public class RpcClientOptions {

    private List<PeerId> peerIds = new ArrayList<>();

    private RpcRemoteOptions rpcRemoteOptions;

    public void addPeerId(PeerId peerId) {
        if(!peerIds.contains(peerId)) {
            peerIds.add(peerId);
        }
    }

    public void addPeerIds(List<PeerId> peerIds) {
        for(PeerId peerId : peerIds) {
            addPeerId(peerId);
        }
    }

    public List<PeerId> getPeerIds() {
        return peerIds;
    }

    public void setRpcRemoteOptions(RpcRemoteOptions rpcRemoteOptions) {
        this.rpcRemoteOptions = rpcRemoteOptions;
    }

    public RpcRemoteOptions getRpcRemoteOptions() {
        return rpcRemoteOptions;
    }
}
