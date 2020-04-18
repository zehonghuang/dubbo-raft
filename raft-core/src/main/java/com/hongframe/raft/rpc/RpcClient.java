package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 19:31
 */
public class RpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

    private RpcRemoteOptions rpcRemoteOptions;

    private Map<PeerId, Map<String, ReferenceConfig>> references = new ConcurrentHashMap<>();

    public RpcClient(RpcRemoteOptions options){
        this.rpcRemoteOptions = options;
    }

    public void init(RpcRemoteOptions options) {
    }

    public boolean connect(PeerId peerId) {
        if(!references.containsKey(peerId)) {
            references.put(peerId, addReferenceConfig(peerId));
        }
        return true;
    }

    private Map<String, ReferenceConfig> addReferenceConfig(PeerId peerId) {
        Map<String, ReferenceConfig> referenceConfigMap = new HashMap<>();
        List<Class> classes = this.rpcRemoteOptions.getServicesInterface();
        for(Class c : classes) {
            URL url = new URL("dubbo", peerId.getEndpoint().getIp(), peerId.getEndpoint().getPort(), c.getName());
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
            reference.setRegistry(new RegistryConfig("N/A"));
            reference.setInterface(c);
            reference.setUrl(url.toFullString());
            reference.setGeneric("true");
            reference.setAsync(true);
//            reference.setMock(null);
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


    public CompletableFuture<?> requestVote(PeerId peerId, RequestVoteRequest request, Callback callback) {
        return invokeAsync(findReferenceConfig(peerId, request), request, callback);
    }

    private static CompletableFuture<?> invokeAsync(ReferenceConfig reference, Message request, Callback callBack) {
        GenericService genericService = (GenericService) reference.get();
        genericService.$invoke(request.method(), new String[] { request.getName() },
                new Object[] { request });
        CompletableFuture<?> future = RpcContext.getContext().getCompletableFuture();

        future.whenComplete((response, e) -> {
            Map<String, Object> map = (Map) response;
            if(e == null) {
                try {
                    callBack.run(mapToResponse(map));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                e.fillInStackTrace();
            }
        });
        return future;
    }

    private static Response mapToResponse(Map<String, Object> map) throws Exception {
        Response response = new Response();
        Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
        if(Objects.nonNull(dataMap)) {
            Message message = (Message) Class.forName((String) dataMap.get("class")).newInstance();
            BeanUtils.populate(message, dataMap);
            response.setData(message);
        }
        Map<String, Object> errorMap = (Map<String, Object>) map.get("error");
        if(Objects.nonNull(errorMap)) {
            ErrorResponse errorResponse = (ErrorResponse) Class.forName((String) errorMap.get("class")).newInstance();
            BeanUtils.populate(errorResponse, errorMap);
            response.setError(errorResponse);
        }
        return response;
    }

}
