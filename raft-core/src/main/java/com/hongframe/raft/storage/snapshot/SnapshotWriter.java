package com.hongframe.raft.storage.snapshot;

import com.google.protobuf.Message;
import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.entity.SnapshotMeta;

import java.io.Closeable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:43
 */
public interface SnapshotWriter extends Snapshot, Closeable, Lifecycle<Void> {

    boolean saveMeta(final SnapshotMeta meta);

    default boolean addFile(final String fileName) {
        return addFile(fileName, null);
    }

    boolean addFile(final String fileName, final Message fileMeta);

}
