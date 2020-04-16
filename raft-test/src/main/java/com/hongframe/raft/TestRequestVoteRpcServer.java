package com.hongframe.raft;

import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.util.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:01
 */
public class TestRequestVoteRpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(TestRequestVoteRpcServer.class);

    public static void main(String[] args) {
        RpcRemoteOptions options = new RpcRemoteOptions();
        options.setNode(new NodeImpl(null, null));
        Endpoint endpoint = new Endpoint("localhost", 8888);
        RpcServer rpcServer = new RpcServer(endpoint, options);

        rpcServer.init();

        RaftGroupService raftGroupService = new RaftGroupService("", new PeerId(endpoint, 0), rpcServer, null);
        Node node = raftGroupService.start();

        LOG.info("started...");
    }

}
