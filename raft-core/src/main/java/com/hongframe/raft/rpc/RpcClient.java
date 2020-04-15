package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcClientOptions;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 19:31
 */
public class RpcClient {

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


    public CompletableFuture<Message> requestVote(PeerId peerId, RequestVoteRequest request, Callback callBack) {
        return invokeAsync(findReferenceConfig(peerId, request), request, callBack);
    }

    private static CompletableFuture<Message> invokeAsync(ReferenceConfig reference, Message request, Callback callBack) {
        CompletableFuture<Message> future = null;
        if (request instanceof RequestVoteRequest) {
            RequestVoteRequest voteRequest = (RequestVoteRequest) request;
            RequestVoteRpc voteRpc = (RequestVoteRpc) reference.get();
            if (voteRequest.isPreVote()) {
                voteRpc.preVote(voteRequest);
            } else {
                voteRpc.requestVote(voteRequest);
            }
        }

        future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((response, e) -> {
            if(e == null) {
                callBack.run(response);
            } else {
                e.fillInStackTrace();
            }
        });
        return future;
    }

}
