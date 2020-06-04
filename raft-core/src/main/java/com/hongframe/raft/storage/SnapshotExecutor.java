package com.hongframe.raft.storage;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.RequestCallback;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.option.SnapshotExecutorOptions;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.storage.snapshot.SnapshotStorage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 14:33
 */
public interface SnapshotExecutor extends Lifecycle<SnapshotExecutorOptions> {

    NodeImpl getNode();

    void installSnapshot(final RpcRequests.InstallSnapshotRequest request,
                         final RequestCallback callback);

    void doSnapshot(final Callback callback);

    SnapshotStorage getSnapshotStorage();

    void interruptDownloadingSnapshots(final long newTerm);

    void join() throws InterruptedException;
}
