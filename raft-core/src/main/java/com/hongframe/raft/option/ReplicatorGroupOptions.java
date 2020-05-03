package com.hongframe.raft.option;

import com.hongframe.raft.core.BallotBox;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.storage.LogManager;

public class ReplicatorGroupOptions {

    private int heartbeatTimeoutMs;
    private int electionTimeoutMs;
    private LogManager logManager;
    private NodeImpl node;
    private RpcClient rpcClient;
    private Scheduler timerManager;
    private BallotBox ballotBox;

    public BallotBox getBallotBox() {
        return ballotBox;
    }

    public void setBallotBox(BallotBox ballotBox) {
        this.ballotBox = ballotBox;
    }

    public Scheduler getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(Scheduler timerManager) {
        this.timerManager = timerManager;
    }

    public int getHeartbeatTimeoutMs() {
        return heartbeatTimeoutMs;
    }

    public void setHeartbeatTimeoutMs(int heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    public int getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public String toString() {
        return "ReplicatorGroupOptions{" +
                "heartbeatTimeoutMs=" + heartbeatTimeoutMs +
                ", electionTimeoutMs=" + electionTimeoutMs +
                ", logManager=" + logManager +
                ", node=" + node +
                ", rpcClient=" + rpcClient +
                '}';
    }
}
