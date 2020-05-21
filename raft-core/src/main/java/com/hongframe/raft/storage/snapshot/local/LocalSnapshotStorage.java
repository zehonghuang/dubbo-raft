package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.SnapshotStorage;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;

import java.io.File;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-21 19:43
 */
public class LocalSnapshotStorage implements SnapshotStorage {

    private static final String TEMP_PATH = "temp";
    private final String path;
    private final RaftOptions raftOptions;

    public LocalSnapshotStorage(String path, RaftOptions raftOptions) {
        this.path = path;
        this.raftOptions = raftOptions;
    }

    @Override
    public SnapshotWriter create() {
        return null;
    }

    @Override
    public SnapshotReader open() {
        return null;
    }

    @Override
    public boolean init(Void opts) {
        final File dir = new File(this.path);
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getPath() {
        return null;
    }
}
