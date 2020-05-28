package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.entity.SnapshotMeta;

import java.io.Closeable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 17:11
 */
public abstract class SnapshotReader extends Snapshot implements Closeable, Lifecycle<Void> {

    public abstract SnapshotMeta load();

    public abstract String generateURIForCopy();

}
