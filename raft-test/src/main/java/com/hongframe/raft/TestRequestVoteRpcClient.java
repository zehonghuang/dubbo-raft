package com.hongframe.raft;

import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RpcClientOptions;
import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.Endpoint;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:31
 */
public class TestRequestVoteRpcClient {

    public static void main(String[] args) {

        RpcClientOptions options = new RpcClientOptions();
        options.setRpcRemoteOptions(new RpcRemoteOptions());

        PeerId peerId = new PeerId(new Endpoint("localhost", 8888), 0);
        options.addPeerId(peerId);
        RpcClient rpcClient = new RpcClient();
        rpcClient.init(options);

        RpcRequests.RequestVoteRequest voteRequest = new RpcRequests.RequestVoteRequest();
        voteRequest.setGroupId("raft");
        voteRequest.setTerm(100L);
        voteRequest.setPeerId("localhost:8080");
        voteRequest.setPreVote(true);

        rpcClient.requestVote(peerId, voteRequest, message -> {
            System.out.println(message);
        });


    }

}
