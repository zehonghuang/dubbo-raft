package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Node;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.AppendEntriesRpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 16:52
 */
public class AppendEntriesRpcImpl implements AppendEntriesRpc {

    private NodeImpl node;

    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        return null;
    }

    @Override
    public void setNode(Node node) {
        this.node = (NodeImpl) node;
    }
}