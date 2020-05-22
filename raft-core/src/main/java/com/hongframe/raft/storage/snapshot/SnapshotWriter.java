package com.hongframe.raft.storage.snapshot;

import com.google.protobuf.Message;
import com.hongframe.raft.Lifecycle;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:43
 */
public interface SnapshotWriter extends Snapshot, Lifecycle<Void> {

    default boolean addFile(final String fileName) {
        return addFile(fileName, null);
    }

    boolean addFile(final String fileName, final Message fileMeta);

}
