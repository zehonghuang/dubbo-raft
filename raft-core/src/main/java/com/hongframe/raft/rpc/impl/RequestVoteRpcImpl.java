package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.entity.RequestVoteRequest;
import com.hongframe.raft.entity.RequestVoteResponse;
import com.hongframe.raft.rpc.RequestVoteRpc;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    @Override
    public RequestVoteResponse preVote(RequestVoteRequest request) {
        AsyncContext asyncContext = RpcContext.startAsync();

        new Thread(() -> {
            // 如果要使用上下文，则必须要放在第一句执行
            asyncContext.signalContextSwitch();
            System.out.println(request);
            RequestVoteResponse voteResponse = new RequestVoteResponse();
            voteResponse.setPreVote(true);
            voteResponse.setTerm(request.getTerm());
            // 写回响应
            asyncContext.write(voteResponse);
        }).start();

        System.out.println("async ...");
        return null;
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return null;
    }
}
