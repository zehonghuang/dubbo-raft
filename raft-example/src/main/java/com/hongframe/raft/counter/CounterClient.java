package com.hongframe.raft.counter;

import com.hongframe.raft.RouteTable;
import com.hongframe.raft.Status;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.counter.rpc.CounterService;
import com.hongframe.raft.counter.rpc.GetValueRequest;
import com.hongframe.raft.counter.rpc.IncrementAndGetRequest;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.ClientService;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:38
 */
public class CounterClient {

    private static final Logger LOG = LoggerFactory.getLogger(CounterClient.class);

    public static void main(String[] args) {
        final Configuration conf = new Configuration();
        conf.parse(CounterRaftServerStartup.NODES);

        RouteTable.getInstance().updateConf(CounterRaftServerStartup.GROUP, conf);
        RpcRemoteOptions options = new RpcRemoteOptions();
        options.registerUserService(CounterService.class, null);
        ClientService clientService = new ClientService(options);
        RouteTable.getInstance().refreshLeader(clientService, CounterRaftServerStartup.GROUP);

        PeerId leader = RouteTable.getInstance().selectLeader(CounterRaftServerStartup.GROUP);

        incrementAndGet(leader, clientService);

        List<PeerId> peerIds = RouteTable.getInstance().getConf(CounterRaftServerStartup.GROUP).getPeers();
        peerIds.remove(leader);

        for(PeerId peerId : peerIds) {
            System.out.println(peerId);
            getValue(peerId, clientService);
        }

    }


    private static void incrementAndGet(PeerId leader, ClientService clientService) {
        if(clientService.connect(leader)) {
            for(int i = 0; i < 10; i++) {
                IncrementAndGetRequest request = new IncrementAndGetRequest();
                request.setValue((i + 1) * 10);
                clientService.invokeAsync(leader, request, new ResponseCallbackAdapter() {
                    @Override
                    public void run(Status status) {
                        System.out.println(getResponse());
                    }
                });
            }
        }
    }

    private static void getValue(PeerId peerId, ClientService clientService) {
        if(clientService.connect(peerId)) {
            GetValueRequest request = new GetValueRequest();
            clientService.invokeAsync(peerId, request, new ResponseCallbackAdapter() {
                @Override
                public void run(Status status) {
                    System.out.println("peer: " + peerId.toString() + " getValue: " + getResponse());
                    LOG.info("getValue:  \nstatus: {}, \npeer: {}, getValue: {}", status, peerId.toString(), getResponse());
                }
            });
        }
    }

}
