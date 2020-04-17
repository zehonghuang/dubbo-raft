package com.hongframe.raft;

import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.option.RpcClientOptions;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.util.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:01
 */
public class RaftServerStartup {

    private static final Logger LOG = LoggerFactory.getLogger(RaftServerStartup.class);


    public static Node startup(int port, String servers) {

        Endpoint endpoint = new Endpoint("localhost", port);
        PeerId serverId = new PeerId(endpoint, 0);


        RpcServer rpcServer = DubboRaftRpcFactory.createRaftRpcServer(endpoint);

        Configuration configuration = new Configuration();
        configuration.parse(servers);

        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setConfig(configuration);

        RaftGroupService raftGroupService = new RaftGroupService("raft", serverId, nodeOptions, rpcServer);
        Node node = raftGroupService.start();
        LOG.info("started...");

        return node;
    }


}
