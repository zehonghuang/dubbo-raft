package com.hongframe.raft;

import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 16:37
 */
public class RaftGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(RaftGroupService.class);

    private volatile boolean started = false;

    private PeerId peerId;

    private RpcServer rpcServer;

    private RpcClient rpcClient;

    private String groupId;

    private Node node;

    private NodeOptions nodeOptions;

    public RaftGroupService(String groupId, PeerId peerId, NodeOptions options, RpcServer rpcServer, RpcClient rpcClient) {
        this.groupId = groupId;
        this.peerId = peerId;
        this.rpcServer = rpcServer;
        this.rpcClient = rpcClient;
        this.nodeOptions = options;
    }

    public synchronized Node start() {
        NodeManager.getInstance().addAddress(this.peerId.getEndpoint());

        this.node = new NodeImpl(this.groupId, this.peerId);
        this.node.init(null);
        this.rpcServer.init();
        return node;
    }

}
