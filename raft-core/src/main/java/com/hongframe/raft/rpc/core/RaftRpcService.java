package com.hongframe.raft.rpc.core;

import com.hongframe.raft.NodeManager;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.RpcService;

import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface RaftRpcService extends RpcService {

    default NodeImpl getNode(Message message) {
        PeerId peerId = new PeerId();
        peerId.parse(message.getPeerId());
        return (NodeImpl) NodeManager.getInstance().get(message.getGroupId(), peerId);
    }

    default Response checkResponse(Message message) {
        if(Objects.nonNull(message))
            return new Response(message);
        else
            return new Response<>(new ErrorResponse());
    }

}
