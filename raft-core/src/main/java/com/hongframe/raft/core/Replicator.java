package com.hongframe.raft.core;

import com.hongframe.raft.option.ReplicatorOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.util.ObjectLock;
import com.hongframe.raft.util.Utils;

public class Replicator {

    private ObjectLock<Replicator> self;
    private final ReplicatorOptions options;
    private RpcClient rpcClient;
    private Scheduler timerManger;
    private volatile long lastRpcSendTimestamp;

    private Replicator(ReplicatorOptions options) {
        this.options = options;
        this.rpcClient = this.options.getRpcClient();
        this.timerManger = this.options.getTimerManager();

    }

    public static ObjectLock<Replicator> start(ReplicatorOptions options) {
        Replicator replicator = new Replicator(options);
        if(!replicator.rpcClient.connect(replicator.options.getPeerId())) {
            return null;
        }
        ObjectLock<Replicator> lock = new ObjectLock<>(replicator);
        lock.lock();
        replicator.lastRpcSendTimestamp = Utils.monotonicMs();
        return lock;
    }

    private void startHeartbeatTimer(int startMs) {

    }


}
