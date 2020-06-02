package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.Status;
import com.hongframe.raft.entity.LocalFileMeta;
import com.hongframe.raft.entity.SnapshotMeta;

import java.util.Set;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:44
 */
public abstract class Snapshot extends Status {

    public static final String RAFT_SNAPSHOT_PREFIX = "snapshot_";

    public static final String RAFT_SNAPSHOT_META_FILE = "__raft_snapshot_meta";

    public static final String REMOTE_SNAPSHOT_URI_SCHEME = "remote://";

    public abstract String getPath();

    public abstract LocalFileMeta getFileMeta(final String fileName);

    public abstract Set<String> listFiles();
}
