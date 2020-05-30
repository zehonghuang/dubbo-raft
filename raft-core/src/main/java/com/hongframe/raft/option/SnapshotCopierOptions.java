package com.hongframe.raft.option;

import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.rpc.RpcClient;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 18:44
 */
public class SnapshotCopierOptions {

    private RpcClient rpcClient;
    private Scheduler timerManager;
    private RaftOptions raftOptions;
    private NodeOptions nodeOptions;

    @Override
    public String toString() {
        return "SnapshotCopierOptions{" +
                "rpcClient=" + rpcClient +
                ", timerManager=" + timerManager +
                ", raftOptions=" + raftOptions +
                ", nodeOptions=" + nodeOptions +
                '}';
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public Scheduler getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(Scheduler timerManager) {
        this.timerManager = timerManager;
    }

    public RaftOptions getRaftOptions() {
        return raftOptions;
    }

    public void setRaftOptions(RaftOptions raftOptions) {
        this.raftOptions = raftOptions;
    }

    public NodeOptions getNodeOptions() {
        return nodeOptions;
    }

    public void setNodeOptions(NodeOptions nodeOptions) {
        this.nodeOptions = nodeOptions;
    }
}
