package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.Lifecycle;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:43
 */
public interface SnapshotStorage extends Lifecycle<Void>, Snapshot {

    SnapshotWriter create();

    SnapshotReader open();
}
