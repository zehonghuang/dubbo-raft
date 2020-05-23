package com.hongframe.raft.storage.snapshot;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:44
 */
public interface Snapshot {

    String RAFT_SNAPSHOT_PREFIX = "snapshot_";

    String RAFT_SNAPSHOT_META_FILE = "__raft_snapshot_meta";

    String REMOTE_SNAPSHOT_URI_SCHEME = "remote://";

    String getPath();
}
