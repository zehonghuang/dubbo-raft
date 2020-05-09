package com.hongframe.raft;

import com.hongframe.raft.core.Replicator;
import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.ReplicatorGroupOptions;
import com.hongframe.raft.util.ObjectLock;

public interface ReplicatorGroup {

    boolean init(final NodeId nodeId, final ReplicatorGroupOptions options);

    boolean addReplicator(final PeerId peer);

    boolean resetTerm(final long newTerm);

    long getLastRpcSendTimestamp(final PeerId peer);

    ObjectLock<Replicator> getReplicator(final PeerId peerId);

    boolean stopReplicator(final PeerId peer);

    boolean stopAll();

    boolean contains(final PeerId peer);

}
