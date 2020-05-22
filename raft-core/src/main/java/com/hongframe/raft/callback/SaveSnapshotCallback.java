package com.hongframe.raft.callback;

import com.hongframe.raft.entity.SnapshotMeta;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-22 17:55
 */
public interface SaveSnapshotCallback extends Callback {

    SnapshotWriter start(final SnapshotMeta meta);

}
