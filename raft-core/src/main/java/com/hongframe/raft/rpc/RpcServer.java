package com.hongframe.raft.rpc;

import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.core.RpcService;
import com.hongframe.raft.rpc.impl.RequestVoteRpcImpl;
import com.hongframe.raft.util.Endpoint;
import com.hongframe.raft.util.NamedThreadFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:11
 */
public class RpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);

    private Integer port;

    private RpcRemoteOptions rpcRemoteOptions;

    private DubboBootstrap dubboBootstrap;



    public RpcServer(Endpoint endpoint, RpcRemoteOptions options) {
        this.port = endpoint.getPort();
        this.rpcRemoteOptions = options;
    }

    public void init() {
        List<ServiceConfig> services = new ArrayList<>();
        try {
            List<Class> servicesInterface = this.rpcRemoteOptions.getServicesInterface();
            List<Class> servicesImpl = this.rpcRemoteOptions.getServicesImpl();
            for (int i = 0; i < servicesInterface.size(); i++) {
                ServiceConfig service = new ServiceConfig<>();
                service.setInterface(servicesInterface.get(i));
                RpcService rpcService = (RpcService) servicesImpl.get(i).newInstance();
                service.setRef(rpcService);
                service.setAsync(true);
                services.add(service);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        this.dubboBootstrap = DubboBootstrap.getInstance();

        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setPort(port);

        new NamedThreadFactory("dubbo-server", false).newThread(() -> {
            dubboBootstrap.application(new ApplicationConfig("dubbo-demo-api-provider"))
                    .registry(new RegistryConfig("N/A"))
                    .services(services)
                    .protocol(protocol)
                    .start()
                    .await();
        }).start();
    }




}
