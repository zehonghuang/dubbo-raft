package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.storage.snapshot.SnapshotCopier;
import com.hongframe.raft.storage.snapshot.SnapshotReader;

import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 17:25
 */
public class LocalSnapshotCopier extends SnapshotCopier {

    private LocalSnapshotStorage storage;
    private boolean filterBeforeCopyRemote;
    private LocalSnapshot remoteSnapshot;

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
