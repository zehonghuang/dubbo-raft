package com.hongframe.raft.core;

import com.hongframe.raft.ReplicatorGroup;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.ReplicatorGroupOptions;
import com.hongframe.raft.util.ObjectLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReplicatorGroupImpl implements ReplicatorGroup {

    private final ConcurrentMap<PeerId, ObjectLock<Replicator>> replicatorMap = new ConcurrentHashMap<>();
    private final Map<PeerId, ReplicatorType> failureReplicators = new ConcurrentHashMap<>();

    private int electionTimeoutMs = -1;

    @Override
    public boolean init(NodeId nodeId, ReplicatorGroupOptions options) {
        return false;
    }

    @Override
    public boolean addReplicator(PeerId peer) {
        return replicatorMap.put(peer, Replicator.start()) == null;
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
