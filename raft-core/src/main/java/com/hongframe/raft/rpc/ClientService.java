package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.ClientRequests.*;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientService extends AbstractRpcClient {

    public ClientService(RpcRemoteOptions options) {
        super(options);
    }

    public GetLeaderResponse getLeader(PeerId peerId, GetLeaderRequest request) {
        return (GetLeaderResponse) invoke(findReferenceConfig(peerId, request), request);
    }

    @Override
    protected Map<String, ReferenceConfig> addReferenceConfig(PeerId peerId) {
        Map<String, ReferenceConfig> referenceConfigMap = new HashMap<>();
        List<Class> classes = this.getRpcRemoteOptions().getClientServicesInterface();
        for (Class c : classes) {
            URL url = new URL("dubbo", peerId.getEndpoint().getIp(), peerId.getEndpoint().getPort(), c.getName());
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
            reference.setRegistry(new RegistryConfig("N/A"));
            reference.setInterface(c);
            reference.setUrl(url.toFullString());
            reference.setGeneric("true");
            reference.setAsync(true);
            reference.setTimeout(1000);
            referenceConfigMap.put(c.getSimpleName(), reference);
        }

        return referenceConfigMap;
    }
}
