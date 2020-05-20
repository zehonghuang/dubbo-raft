package com.hongframe.raft.storage.snapshot;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:44
 */
public interface Snapshot {

    String JRAFT_SNAPSHOT_PREFIX = "snapshot_";

    String REMOTE_SNAPSHOT_URI_SCHEME = "remote://";

    String getPath();
}
