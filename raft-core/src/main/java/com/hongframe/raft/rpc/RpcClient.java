package com.hongframe.raft.rpc;

import com.hongframe.raft.callback.Invokeable;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 19:31
 */
public class RpcClient extends AbstractRpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

    public RpcClient(RpcRemoteOptions options) {
        super(options);
    }

    public void init(RpcRemoteOptions options) {
    }

    public CompletableFuture<?> requestVote(PeerId peerId, RequestVoteRequest request, Invokeable callback) {
        return invokeAsync(findReferenceConfig(peerId, request), request, callback);
    }

    public CompletableFuture<?> appendEntries(PeerId peerId, AppendEntriesRequest request, Invokeable callback) {
        return invokeAsync(findReferenceConfig(peerId, request), request, callback);
    }

    protected Map<String, ReferenceConfig> addReferenceConfig(PeerId peerId) {
        Map<String, ReferenceConfig> referenceConfigMap = new HashMap<>();
        List<Class> classes = this.getRpcRemoteOptions().getServicesInterface();
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
