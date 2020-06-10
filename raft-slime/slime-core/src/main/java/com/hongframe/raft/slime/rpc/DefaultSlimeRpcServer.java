package com.hongframe.raft.slime.rpc;

import com.hongframe.raft.slime.options.RpcOptions;
import com.hongframe.raft.util.NamedThreadFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 17:44
 */
public class DefaultSlimeRpcServer implements SlimeRpcServer {

    private final int port;

    public DefaultSlimeRpcServer(int port) {
        this.port = port;
    }

    private DubboBootstrap dubboBootstrap;

    @Override
    public boolean init(RpcOptions opts) {
        ServiceConfig service = new ServiceConfig<>();
        service.setInterface(KVCommandService.class);
        service.setRef(new KVCommandServiceImpl());
        service.setAsync(true);
        service.setTimeout(opts.getRpcTimeoutMillis());

        this.dubboBootstrap = DubboBootstrap.getInstance();
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setPort(port);

        new NamedThreadFactory("dubbo-server", false).newThread(() -> {
            dubboBootstrap.application(new ApplicationConfig("dubbo-demo-api-provider"))
                    .registry(new RegistryConfig("N/A"))
                    .service(service)
                    .protocol(protocol)
                    .start()
                    .await();
        }).start();

        return true;
    }

    @Override
    public void shutdown() {

    }
}
