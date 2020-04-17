package com.hongframe.raft;

import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.util.Endpoint;


/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 08:07
 */
public class DubboRaftRpcFactory {

    private static final RpcRemoteOptions REMOTE_OPTIONS = new RpcRemoteOptions();

    public static RpcServer createRaftRpcServer(final Endpoint endpoint) {
        return new RpcServer(endpoint, REMOTE_OPTIONS);
    }

    public static RpcClient createRaftRpcClient() {
        RpcClient rpcClient = new RpcClient(REMOTE_OPTIONS);
        return rpcClient;
    }



}
