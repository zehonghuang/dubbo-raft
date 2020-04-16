package com.hongframe.raft;

import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.option.RaftOptions;

public interface Node extends Lifecycle<RaftOptions> {

    NodeId getNodeId();

}
