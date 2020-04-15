package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;

import java.util.concurrent.CompletableFuture;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 19:31
 */
public class RpcClient {

    private RpcServer rpcServer;

    public RpcClient(RpcServer rpcServer){
        this.rpcServer = rpcServer;
    }



    public CompletableFuture<Message> requestVote(RequestVoteRequest request, Callback callBack) {
        return invokeAsync(null, request, callBack);
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
