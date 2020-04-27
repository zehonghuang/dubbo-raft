package com.hongframe.raft;

import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.Endpoint;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:31
 */
public class TestRequestVoteRpcClient {

    public static void main(String[] args) {

        PeerId peerId = new PeerId(new Endpoint("localhost", 8888), 0);

        RpcClient rpcClient = DubboRaftRpcFactory.createRaftRpcClient();

        rpcClient.connect(peerId);

        RpcRequests.RequestVoteRequest voteRequest = new RpcRequests.RequestVoteRequest();
        voteRequest.setGroupId("raft");
        voteRequest.setTerm(100L);
        voteRequest.setPeerId("localhost:8888");
        voteRequest.setPreVote(true);

        try {
            System.out.println(rpcClient.requestVote(peerId, voteRequest, new ResponseCallbackAdapter() {
                @Override
                public void run(Status status) {
                    getResponse();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
