package com.hongframe.raft.counter;

import com.hongframe.raft.DubboRaftRpcFactory;
import com.hongframe.raft.Node;
import com.hongframe.raft.RaftGroupService;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.counter.rpc.CounterService;
import com.hongframe.raft.counter.rpc.CounterServiceImpl;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.util.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:32
 */
public class CounterRaftServerStartup {

    private static final Logger LOG = LoggerFactory.getLogger(CounterRaftServerStartup.class);

    public static final String NODES = "localhost:8888,localhost:8889,localhost:8890,localhost:8891,localhost:8892";

    public static final String GROUP = "raft";

    private Node node;


    private Node startup(int port, String servers) {

        Endpoint endpoint = new Endpoint("localhost", port);
        PeerId serverId = new PeerId(endpoint, 0);


        RpcServer rpcServer = DubboRaftRpcFactory.createRaftRpcServer(endpoint);
        rpcServer.registerUserService(CounterService.class, new CounterServiceImpl(this));

        Configuration configuration = new Configuration();
        configuration.parse(servers);

        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setConfig(configuration);
        nodeOptions.setLogUri(".");

        RaftGroupService raftGroupService = new RaftGroupService(GROUP, serverId, nodeOptions, rpcServer);
        this.node = raftGroupService.start();
        LOG.info("started...");

        return node;
    }

    private CounterRaftServerStartup(int port, String servers) {
        this.node = startup(port, servers);
    }

    public static CounterRaftServerStartup create(int port, String servers) {
        return new CounterRaftServerStartup(port, servers);
    }

    public Node getNode() {
        return node;
    }
}
