package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.storage.snapshot.SnapshotCopier;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.remote.RemoteFileCopier;

import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 17:25
 */
public class LocalSnapshotCopier extends SnapshotCopier {

    private boolean cancelled;
    private LocalSnapshotStorage storage;
    private boolean filterBeforeCopyRemote;
    private LocalSnapshot remoteSnapshot;
    private RemoteFileCopier copier;

    public boolean init(final String uri, final SnapshotCopierOptions opts) {
        this.copier = new RemoteFileCopier();
        this.cancelled = false;
        this.filterBeforeCopyRemote = opts.getNodeOptions().isFilterBeforeCopyRemote();
        this.remoteSnapshot = new LocalSnapshot(opts.getRaftOptions());
        return this.copier.init(uri, opts);
    }

    @Override
    public void cancel() {

    }

    @Override
    public void join() throws InterruptedException {

    }

    @Override
    public void start() {

    }

    @Override
    public SnapshotReader getReader() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    public LocalSnapshotStorage getStorage() {
        return storage;
    }

    public void setStorage(LocalSnapshotStorage storage) {
        this.storage = storage;
    }
}
