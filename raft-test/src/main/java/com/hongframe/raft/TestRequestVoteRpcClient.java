package com.hongframe.raft;

import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.RpcRequests;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:31
 */
public class TestRequestVoteRpcClient {

    public static void main(String[] args) {
        URL url = new URL("dubbo", "localhost", 8888, RequestVoteRpc.class.getName());

        ReferenceConfig<RequestVoteRpc> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
        reference.setRegistry(new RegistryConfig("N/A"));
        reference.setInterface(RequestVoteRpc.class);
        reference.setUrl(url.toFullString());
//        reference.setAsync(true);


        RpcRequests.RequestVoteRequest voteRequest = new RpcRequests.RequestVoteRequest();
        voteRequest.setGroupId("raft");
        voteRequest.setTerm(100L);
        voteRequest.setPeerId("localhost:8080");
        voteRequest.setPreVote(true);

        System.out.println(reference.get().preVote(voteRequest));



    }

}
