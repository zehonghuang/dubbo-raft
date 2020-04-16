package com.hongframe.raft.rpc;

import com.hongframe.raft.NodeManager;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcClientOptions;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 19:31
 */
public class RpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

    private RpcClientOptions rpcClientOptions;

    private Map<PeerId, Map<String, ReferenceConfig>> references = new HashMap<>();

    public RpcClient(){
    }

    public void init(RpcClientOptions options) {
        this.rpcClientOptions = options;
        for(PeerId peerId : this.rpcClientOptions.getPeerIds()) {
            references.put(peerId, addReferenceConfig(peerId));
        }

    }

    private Map<String, ReferenceConfig> addReferenceConfig(PeerId peerId) {
        Map<String, ReferenceConfig> referenceConfigMap = new HashMap<>();
        List<Class> classes = this.rpcClientOptions.getRpcRemoteOptions().getServicesInterface();
        for(Class c : classes) {
            URL url = new URL("dubbo", peerId.getEndpoint().getIp(), peerId.getEndpoint().getPort(), c.getName());
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
            reference.setRegistry(new RegistryConfig("N/A"));
            reference.setInterface(c);
            reference.setUrl(url.toFullString());
            reference.setGeneric("true");
            reference.setAsync(true);
            referenceConfigMap.put(c.getSimpleName(), reference);
        }

        return referenceConfigMap;
    }

    private ReferenceConfig findReferenceConfig(PeerId peerId, Message message) {
        if(StringUtils.isNotBlank(message.seviceName())) {
            return references.get(peerId).get(message.seviceName());
        }
        return null;
    }


    public CompletableFuture<?> requestVote(PeerId peerId, RequestVoteRequest request) {
        return invokeAsync(findReferenceConfig(peerId, request), request, (message) -> {
            NodeImpl node = (NodeImpl) NodeManager.getInstance().get(request.getGroupId(), peerId);
            node.handleVoteResponse((RequestVoteResponse) message);
        });
    }

    private static CompletableFuture<?> invokeAsync(ReferenceConfig reference, Message request, Callback callBack) {
        GenericService genericService = (GenericService) reference.get();
        genericService.$invoke(request.method(), new String[] { request.getName() },
                new Object[] { request });
        CompletableFuture<?> future = RpcContext.getContext().getCompletableFuture();

        future.whenComplete((response, e) -> {
            Map<String, String> map = (Map) response;
            if(e == null) {
                try {
                    Object o = Class.forName(map.get("class")).newInstance();
                    BeanUtils.populate(o, map);
                    callBack.run((Message) o);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                e.fillInStackTrace();
            }
        });
        return future;
    }

}
