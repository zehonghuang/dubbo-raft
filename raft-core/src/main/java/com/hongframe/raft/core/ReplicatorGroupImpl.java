package com.hongframe.raft.core;

import com.hongframe.raft.ReplicatorGroup;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.ReplicatorGroupOptions;
import com.hongframe.raft.option.ReplicatorOptions;
import com.hongframe.raft.util.ObjectLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReplicatorGroupImpl implements ReplicatorGroup {

    private final ConcurrentMap<PeerId, ObjectLock<Replicator>> replicatorMap = new ConcurrentHashMap<>();
    private final Map<PeerId, ReplicatorType> failureReplicators = new ConcurrentHashMap<>();

    private int electionTimeoutMs = -1;
    private NodeId nodeId;
    private ReplicatorGroupOptions options;
    private ReplicatorOptions replicatorOptions;

    @Override
    public boolean init(NodeId nodeId, ReplicatorGroupOptions options) {
        this.nodeId = nodeId;
        this.options = options;

        this.replicatorOptions = new ReplicatorOptions();
        this.replicatorOptions.setServerId(this.nodeId.getPeerId());
        this.replicatorOptions.setGroupId(this.nodeId.getGroupId());
        this.replicatorOptions.setLogManager(this.options.getLogManager());
        this.replicatorOptions.setRpcClient(this.options.getRpcClient());
        this.replicatorOptions.setTimerManager(this.options.getTimerManager());
        this.replicatorOptions.setTerm(0);
        this.replicatorOptions.setNode(this.options.getNode());
        this.replicatorOptions.setElectionTimeoutMs(this.options.getElectionTimeoutMs());
        this.replicatorOptions.setDynamicHeartBeatTimeoutMs(this.options.getHeartbeatTimeoutMs());
        return true;
    }

    @Override
    public boolean addReplicator(PeerId peer) {
        ReplicatorOptions ro = this.replicatorOptions.copy();
        ro.setPeerId(peer);
        return replicatorMap.put(peer, Replicator.start(ro)) == null;
    }

    @Override
    public boolean resetTerm(long newTerm) {
        return false;
    }

    @Override
    public ObjectLock<Replicator> getReplicator(PeerId peerId) {
        return null;
    }

    @Override
    public boolean stopReplicator(PeerId peer) {
        return false;
    }

    @Override
    public boolean stopAll() {
        return false;
    }

    @Override
    public boolean contains(PeerId peer) {
        return false;
    }
}
