package com.hongframe.raft.option;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.core.NodeImpl;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 14:53
 */
public class ReadOnlyServiceOptions {

    private RaftOptions raftOptions;
    private NodeImpl node;
    private FSMCaller fsmCaller;

    public RaftOptions getRaftOptions() {
        return raftOptions;
    }

    public void setRaftOptions(RaftOptions raftOptions) {
        this.raftOptions = raftOptions;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public FSMCaller getFsmCaller() {
        return fsmCaller;
    }

    public void setFsmCaller(FSMCaller fsmCaller) {
        this.fsmCaller = fsmCaller;
    }
}
