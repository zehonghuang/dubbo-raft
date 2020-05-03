package com.hongframe.raft.option;

import com.hongframe.raft.core.BallotBox;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.util.Copiable;

public class ReplicatorOptions implements Copiable<ReplicatorOptions> {

    private int dynamicHeartBeatTimeoutMs;
    private int electionTimeoutMs;
    private String groupId;
    private PeerId serverId;
    private PeerId peerId;
    private LogManager logManager;
    private NodeImpl node;
    private long term;
    private BallotBox ballotBox;
    private RpcClient rpcClient;
    private Scheduler timerManager;

    public ReplicatorOptions() {
    }

    public ReplicatorOptions(int dynamicHeartBeatTimeoutMs, int electionTimeoutMs, String groupId, PeerId serverId,
                             PeerId peerId, LogManager logManager, NodeImpl node, long term, RpcClient rpcClient,
                             Scheduler timerManager, BallotBox ballotBox) {
        this.dynamicHeartBeatTimeoutMs = dynamicHeartBeatTimeoutMs;
        this.electionTimeoutMs = electionTimeoutMs;
        this.groupId = groupId;
        this.serverId = serverId;
        this.peerId = peerId;
        this.logManager = logManager;
        this.node = node;
        this.term = term;
        this.rpcClient = rpcClient;
        this.timerManager = timerManager;
    }

    @Override
    public ReplicatorOptions copy() {
        return new ReplicatorOptions(this.dynamicHeartBeatTimeoutMs, this.electionTimeoutMs, this.groupId, this.serverId,
                this.peerId, this.logManager, this.node, this.term, this.rpcClient, this.timerManager, this.ballotBox);
    }

    public Scheduler getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(Scheduler timerManager) {
        this.timerManager = timerManager;
    }

    public int getDynamicHeartBeatTimeoutMs() {
        return dynamicHeartBeatTimeoutMs;
    }

    public void setDynamicHeartBeatTimeoutMs(int dynamicHeartBeatTimeoutMs) {
        this.dynamicHeartBeatTimeoutMs = dynamicHeartBeatTimeoutMs;
    }

    public int getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public PeerId getServerId() {
        return serverId;
    }

    public void setServerId(PeerId serverId) {
        this.serverId = serverId;
    }

    public PeerId getPeerId() {
        return peerId;
    }

    public void setPeerId(PeerId peerId) {
        this.peerId = peerId;
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

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public BallotBox getBallotBox() {
        return ballotBox;
    }

    public void setBallotBox(BallotBox ballotBox) {
        this.ballotBox = ballotBox;
    }
}
