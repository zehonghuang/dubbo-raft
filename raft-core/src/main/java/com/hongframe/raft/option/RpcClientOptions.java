package com.hongframe.raft.option;

import com.hongframe.raft.entity.PeerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 22:50
 */
public class RpcClientOptions {

    private List<PeerId> peerIds = new ArrayList<>();

    private RpcRemoteOptions rpcRemoteOptions;

    public void addPeerId(PeerId peerId) {
        if(Objects.equals(peerId, this.rpcRemoteOptions.getServerId())) {
            return;
        }
        if(!peerIds.contains(peerId)) {
            peerIds.add(peerId);
        }
    }

    public void addPeerIds(List<PeerId> peerIds) {
        for(PeerId peerId : peerIds) {
            if(Objects.equals(peerId, this.rpcRemoteOptions.getServerId())) {
                continue;
            }
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
