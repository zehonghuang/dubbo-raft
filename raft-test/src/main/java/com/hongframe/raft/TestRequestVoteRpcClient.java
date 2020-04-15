package com.hongframe.raft;

import com.hongframe.raft.entity.RequestVoteRequest;
import com.hongframe.raft.entity.RequestVoteResponse;
import com.hongframe.raft.rpc.RequestVoteRpc;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 18:31
 */
public class TestRequestVoteRpcClient {

    public static void main(String[] args) {
        ReferenceConfig<RequestVoteRpc> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
        reference.setRegistry(new RegistryConfig("N/A"));
        reference.setInterface(RequestVoteRpc.class);
        reference.setUrl("dubbo://localhost:20880/com.hongframe.raft.rpc.RequestVoteRpc");
        RequestVoteRpc service = reference.get();

        RequestVoteRequest voteRequest = new RequestVoteRequest();
        voteRequest.setGroupId("raft");
        voteRequest.setTerm(100L);
        voteRequest.setPeerId("localhost:8080");
        RequestVoteResponse message = service.preVote(voteRequest);
        System.out.println(message);
    }

}
