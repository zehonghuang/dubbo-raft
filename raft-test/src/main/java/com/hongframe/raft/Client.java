package com.hongframe.raft;

import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.ClientService;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 21:35
 */
public class Client {

    public static void main(String[] args) {
        final Configuration conf = new Configuration();
        conf.parse(RaftServerStartup.NODES);

        RouteTable.getInstance().updateConf(RaftServerStartup.GROUP, conf);

        ClientService clientService = new ClientService(new RpcRemoteOptions());
        RouteTable.getInstance().refreshLeader(clientService, RaftServerStartup.GROUP);

        System.out.println(RouteTable.getInstance().selectLeader(RaftServerStartup.GROUP));
    }

}
