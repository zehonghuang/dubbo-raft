package com.hongframe.raft.core;

import com.hongframe.raft.ReplicatorGroup;
import com.hongframe.raft.callback.ResponseCallback;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.ReplicatorGroupOptions;
import com.hongframe.raft.option.ReplicatorOptions;
import com.hongframe.raft.util.ObjectLock;

import java.util.ArrayList;
import java.util.List;
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
        this.replicatorOptions.setBallotBox(this.options.getBallotBox());
        this.replicatorOptions.setRaftOptions(this.options.getRaftOptions());
        return true;
    }

    @Override
    public boolean addReplicator(PeerId peer) {
        ReplicatorOptions ro = this.replicatorOptions.copy();
        ro.setPeerId(peer);
        return replicatorMap.put(peer, Replicator.start(ro)) == null;
    }

    @Override
    public void sendHeartbeat(PeerId peer, ResponseCallback callback) {
        final ObjectLock<Replicator> lock = this.replicatorMap.get(peer);
        if(lock == null) {
            //TODO sendHeartbeat npe
            return;
        }
        Replicator.sendHeartbeat(lock, callback);
    }

    @Override
    public boolean resetTerm(long newTerm) {
        if(newTerm <= this.replicatorOptions.getTerm()) {
            return false;
        }
        this.replicatorOptions.setTerm(newTerm);
        return true;
    }

    @Override
    public long getLastRpcSendTimestamp(PeerId peer) {
        final ObjectLock<Replicator> objectLock = this.replicatorMap.get(peer);
        if (objectLock == null) {
            return 0L;
        }
        return Replicator.getLastRpcSendTimestamp(objectLock);
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
        final List<ObjectLock<Replicator>> rids = new ArrayList<>(this.replicatorMap.values());
        this.replicatorMap.clear();
        this.failureReplicators.clear();
        for (final ObjectLock rid : rids) {
            Replicator.stop(rid);
        }
        return true;
    }

    @Override
    public boolean contains(PeerId peer) {
        return false;
    }
}
