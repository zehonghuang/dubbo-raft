package com.hongframe.raft.storage.snapshot.remote;

import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.util.Endpoint;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-31 10:31
 */
public class RemoteFileCopier {

    private long readId;
    private RpcClient rpcClient;
    private Endpoint endpoint;
    private RaftOptions raftOptions;
    private Scheduler timerManager;

    public boolean init(String uri, final SnapshotCopierOptions opts) {
        this.rpcClient = opts.getRpcClient();
        this.timerManager = opts.getTimerManager();
        this.raftOptions = opts.getRaftOptions();
        return true;
    }

}
