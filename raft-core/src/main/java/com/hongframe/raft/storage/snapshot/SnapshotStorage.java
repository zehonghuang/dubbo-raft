package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.storage.snapshot.local.LocalSnapshotWriter;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 15:43
 */
public abstract class SnapshotStorage extends Snapshot implements Lifecycle<Void> {

    public abstract SnapshotWriter create();

    public abstract SnapshotReader open();

    public abstract SnapshotCopier startToCopyFrom(final String uri, final SnapshotCopierOptions opts);

    public abstract void close(LocalSnapshotWriter writer, boolean keepDataOnError);
}
