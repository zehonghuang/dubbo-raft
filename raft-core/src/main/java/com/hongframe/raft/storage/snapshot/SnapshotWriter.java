package com.hongframe.raft.storage.snapshot;

import com.google.protobuf.Message;
import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.entity.SnapshotMeta;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:43
 */
public abstract class SnapshotWriter extends Snapshot implements Closeable, Lifecycle<Void> {

    public abstract boolean saveMeta(final SnapshotMeta meta);

    public boolean addFile(final String fileName) {
        return addFile(fileName, null);
    }

    public abstract boolean addFile(final String fileName, final com.hongframe.raft.entity.LocalFileMeta fileMeta);

    public abstract boolean removeFile(final String fileName);

    public abstract void close(final boolean keepDataOnError) throws IOException;

}
