package com.hongframe.raft.core;

import com.hongframe.raft.Node;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RaftOptions;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 01:40
 */
public class NodeImpl implements Node {

    private PeerId serverId;
    private PeerId leaderId;

    @Override
    public boolean init(RaftOptions opts) {
        return false;
    }

    public void handlePreVoteRequest(RequestVoteRequest voteRequest) {

    }

    public void handlePreVoteResponse() {

    }



    @Override
    public void shutdown() {

    }

}
