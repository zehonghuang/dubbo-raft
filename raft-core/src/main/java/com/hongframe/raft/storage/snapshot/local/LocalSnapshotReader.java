package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.entity.SnapshotMeta;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-28 14:45
 */
public class LocalSnapshotReader extends SnapshotReader {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotReader.class);

    private long readerId;
    private final LocalSnapshotMetaTable metaTable;
    private final String path;
    private final LocalSnapshotStorage snapshotStorage;

    public LocalSnapshotReader(String path, LocalSnapshotStorage snapshotStorage, RaftOptions raftOptions) {
        this.path = path;
        this.snapshotStorage = snapshotStorage;
        this.readerId = 0;
        this.metaTable = new LocalSnapshotMetaTable(raftOptions);
    }

    @Override
    public boolean init(Void opts) {
        final File dir = new File(this.path);
        if (!dir.exists()) {
            LOG.error("No such path %s for snapshot reader.", this.path);
            setError(10001, "No such path %s for snapshot reader", this.path);
            return false;
        }
        final String metaPath = this.path + File.separator + RAFT_SNAPSHOT_META_FILE;
        try {
            return this.metaTable.loadFromFile(metaPath);
        } catch (final IOException e) {
            LOG.error("Fail to load snapshot meta {}.", metaPath);
            setError(10001, "Fail to load snapshot meta from path %s", metaPath);
            return false;
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public SnapshotMeta load() {
        if (this.metaTable.hasMeta()) {
            return this.metaTable.getMeta();
        }
        return null;
    }

    @Override
    public String generateURIForCopy() {
        return null;
    }

    private long getSnapshotIndex() {
        final File file = new File(this.path);
        final String name = file.getName();
        if (!name.startsWith(RAFT_SNAPSHOT_PREFIX)) {
            throw new IllegalStateException("Invalid snapshot path name:" + name);
        }
        return Long.parseLong(name.substring(RAFT_SNAPSHOT_PREFIX.length()));
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void close() throws IOException {
        snapshotStorage.unref(this.getSnapshotIndex());
    }
}
