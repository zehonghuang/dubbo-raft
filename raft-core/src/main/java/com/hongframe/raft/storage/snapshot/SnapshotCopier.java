package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.Status;
import com.hongframe.raft.storage.snapshot.SnapshotReader;

import java.io.Closeable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 16:59
 */
public abstract class SnapshotCopier extends Status implements Closeable {

    public abstract void cancel();

    public abstract void join() throws InterruptedException;

    public abstract void start();

    public abstract SnapshotReader getReader();
}
