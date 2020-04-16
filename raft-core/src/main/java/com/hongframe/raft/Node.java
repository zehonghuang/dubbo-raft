package com.hongframe.raft;

import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.option.NodeOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface Node extends Lifecycle<NodeOptions> {

    NodeId getNodeId();

}
