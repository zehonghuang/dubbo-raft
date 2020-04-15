package com.hongframe.raft;

import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.RequestVoteRpcImpl;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:01
 */
public class TestRequestVoteRpcServer {

    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer(8888);

        rpcServer.init();

        System.out.println("started");
    }

}
