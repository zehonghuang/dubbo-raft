package com.hongframe.raft.rpc;

import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.core.RaftRpcService;
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

    public void registerUserService(Class serviceInterface, Class serviceImpl) {
        this.rpcRemoteOptions.registerUserService(serviceInterface, serviceImpl);
    }

    public void init() {
        List<ServiceConfig> services = new ArrayList<>();
        try {
            List<Class> servicesInterface = this.rpcRemoteOptions.getServicesInterface();
            List<Class> servicesImpl = this.rpcRemoteOptions.getServicesImpl();
            for (int i = 0; i < servicesInterface.size(); i++) {
                services.add(createServiceConfig(servicesInterface.get(i), servicesImpl.get(i)));
            }
            List<Class> clientServiceInterface = this.rpcRemoteOptions.getClientServicesInterface();
            List<Class> clientServiceImpl = this.rpcRemoteOptions.getClientServicesImpl();
            for (int i = 0; i < clientServiceInterface.size(); i++) {
                services.add(createServiceConfig(clientServiceInterface.get(i), clientServiceImpl.get(i)));
            }

            List<Class> userServiceInterface = this.rpcRemoteOptions.getUserServicesInterface();
            List<Class> userServiceImpl = this.rpcRemoteOptions.getUserServicesImpl();
            for (int i = 0; i < userServiceInterface.size(); i++) {
                services.add(createServiceConfig(userServiceInterface.get(i), userServiceImpl.get(i)));
            }
        } catch (Exception e) {
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

    private ServiceConfig createServiceConfig(Class interfacez, Class implz) throws Exception {
        ServiceConfig service = new ServiceConfig<>();
        service.setInterface(interfacez);
        Object rpcService =  implz.newInstance();
        service.setRef(rpcService);
        service.setAsync(true);
        return service;
    }




}
