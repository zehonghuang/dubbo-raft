package com.hongframe.raft.callback;

import com.hongframe.raft.storage.snapshot.SnapshotReader;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-28 16:18
 */
public interface LoadSnapshotCallback extends Callback {

    SnapshotReader start();

}
