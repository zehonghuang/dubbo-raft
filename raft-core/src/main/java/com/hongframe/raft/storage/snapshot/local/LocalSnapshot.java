package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.entity.LocalFileMeta;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.Snapshot;

import java.util.Set;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-31 11:09
 */
public class LocalSnapshot extends Snapshot {

    private final LocalSnapshotMetaTable metaTable;

    public LocalSnapshot(RaftOptions raftOptions) {
        this.metaTable = new LocalSnapshotMetaTable(raftOptions);
    }

    public LocalSnapshotMetaTable getMetaTable() {
        return this.metaTable;
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException();
    }

    /**
     * List all the existing files in the Snapshot currently
     *
     * @return the existing file list
     */
    @Override
    public Set<String> listFiles() {
        return this.metaTable.listFiles();
    }

    @Override
    public LocalFileMeta getFileMeta(final String fileName) {
        return this.metaTable.getFileMeta(fileName);
    }

}
