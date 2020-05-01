package com.hongframe.raft.counter;

import com.hongframe.raft.RouteTable;
import com.hongframe.raft.Status;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.counter.rpc.CounterService;
import com.hongframe.raft.counter.rpc.IncrementAndGetRequest;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.ClientService;
import com.hongframe.raft.callback.ResponseCallbackAdapter;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:38
 */
public class CounterClient {

    public static void main(String[] args) {
        final Configuration conf = new Configuration();
        conf.parse(CounterRaftServerStartup.NODES);

        RouteTable.getInstance().updateConf(CounterRaftServerStartup.GROUP, conf);
        RpcRemoteOptions options = new RpcRemoteOptions();
        options.registerUserService(CounterService.class, null);
        ClientService clientService = new ClientService(options);
        RouteTable.getInstance().refreshLeader(clientService, CounterRaftServerStartup.GROUP);

        PeerId leader = RouteTable.getInstance().selectLeader(CounterRaftServerStartup.GROUP);

        IncrementAndGetRequest request = new IncrementAndGetRequest();
        request.setValue(11111);
        System.out.println(leader);
        if(clientService.connect(leader)) {
            for(int i = 0; i < 110; i++) {
                clientService.invokeAsync(leader, request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        System.out.println(getResponse());
                    }
                });
            }
        }
    }

}
