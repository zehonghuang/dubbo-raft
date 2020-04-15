package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.entity.RequestVoteRequest;
import com.hongframe.raft.entity.RequestVoteResponse;
import com.hongframe.raft.rpc.RequestVoteRpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:58
 */
public class RequestVoteRpcImpl implements RequestVoteRpc {

    public RequestVoteResponse preVote(RequestVoteRequest request) {
        System.out.println(request);
        RequestVoteResponse voteResponse = new RequestVoteResponse();
        voteResponse.setPreVote(true);
        voteResponse.setTerm(request.getTerm());
        return voteResponse;
    }

}
