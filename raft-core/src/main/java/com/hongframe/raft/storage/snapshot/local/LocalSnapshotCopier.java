package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.storage.snapshot.SnapshotCopier;
import com.hongframe.raft.storage.snapshot.SnapshotReader;

import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 17:25
 */
public class LocalSnapshotCopier extends SnapshotCopier {
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
}
