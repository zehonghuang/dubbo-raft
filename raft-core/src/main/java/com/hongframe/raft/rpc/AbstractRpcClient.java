package com.hongframe.raft.rpc;

import com.hongframe.raft.callback.Invokeable;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcRemoteOptions;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRpcClient.class);

    private RpcRemoteOptions rpcRemoteOptions;

    private Map<PeerId, Map<String, ReferenceConfig>> references = new ConcurrentHashMap<>();

    public AbstractRpcClient(RpcRemoteOptions rpcRemoteOptions) {
        this.rpcRemoteOptions = rpcRemoteOptions;
    }

    public Map<PeerId, Map<String, ReferenceConfig>> getReferences() {
        return references;
    }

    public RpcRemoteOptions getRpcRemoteOptions() {
        return rpcRemoteOptions;
    }

    public boolean connect(PeerId peerId) {
        if (!getReferences().containsKey(peerId)) {
            getReferences().put(peerId, addReferenceConfig(peerId));
        }
        return true;
    }

    public CompletableFuture<?> invokeAsync(PeerId peerId, Message request, Invokeable callback) {
        return invokeAsync(findReferenceConfig(peerId, request), request, callback);
    }

    public Message invoke(ReferenceConfig reference, Message request) {
        try {
            Map<String, String> res = (Map<String, String>) invokeAsync(reference, request, null, false).get();
            Object o = Class.forName(res.get("class")).newInstance();
            BeanUtils.populate(o, res);
            return (Message) o;
        } catch (Exception e) {
            LOG.error("get message fail {}", request.getPeerId(), e);
        }
        return null;
    }

    protected static CompletableFuture<?> invokeAsync(ReferenceConfig reference, Message request, Invokeable callBack) {
        return invokeAsync(reference, request, callBack, true);
    }

    private static CompletableFuture<?> invokeAsync(ReferenceConfig reference, Message request, Invokeable callBack, boolean isAsync) {
        GenericService genericService;
        CompletableFuture<?> future = null;
        try {
            genericService = (GenericService) reference.get();
            genericService.$invoke(request.method(), new String[]{request.getName()},
                    new Object[]{request});
            future = RpcContext.getContext().getCompletableFuture();
            if (isAsync) {
                future.whenComplete((response, e) -> {
                    Map<String, Object> map = (Map) response;
                    if (e == null) {
                        try {
                            callBack.invoke(mapToResponse(map));
                        } catch (Exception e1) {
                            callBack.invoke(new RpcRequests.Response(new RpcRequests.ErrorResponse(10001, e1.toString())));
                            LOG.error("invoke {} -> {} fail", request.getPeerId(), request.method(), e1);
                        }
                    } else {
                        callBack.invoke(new RpcRequests.Response(new RpcRequests.ErrorResponse(10001, e.toString())));
                        LOG.error("channel {} -> {} fail", request.getPeerId(), request.method(), e);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("", e);
            if (isAsync) {
                callBack.invoke(new RpcRequests.Response(new RpcRequests.ErrorResponse(10001, e.toString())));
            } else {
                future = CompletableFuture.completedFuture(new RpcRequests.Response(new RpcRequests.ErrorResponse(10001, e.toString())));
            }
        }
        return future;
    }

    private static RpcRequests.Response mapToResponse(Map<String, Object> map) throws Exception {
        RpcRequests.Response response = new RpcRequests.Response();
        Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
        if (Objects.nonNull(dataMap)) {
            Message message = (Message) Class.forName((String) dataMap.get("class")).newInstance();
            BeanUtils.populate(message, dataMap);
            response.setData(message);
        }
        Map<String, Object> errorMap = (Map<String, Object>) map.get("error");
        if (Objects.nonNull(errorMap)) {
            RpcRequests.ErrorResponse errorResponse = (RpcRequests.ErrorResponse) Class.forName((String) errorMap.get("class")).newInstance();
            BeanUtils.populate(errorResponse, errorMap);
            response.setError(errorResponse);
        }
        return response;
    }

    protected abstract Map<String, ReferenceConfig> addReferenceConfig(PeerId peerId);

    protected ReferenceConfig findReferenceConfig(PeerId peerId, Message message) {
        if (StringUtils.isNotBlank(message.seviceName())) {
            return references.get(peerId).get(message.seviceName());
        }
        return null;
    }

}
