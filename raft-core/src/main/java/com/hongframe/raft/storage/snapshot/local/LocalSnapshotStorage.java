package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.Snapshot;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.SnapshotStorage;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-21 19:43
 */
public class LocalSnapshotStorage implements SnapshotStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotStorage.class);

    private static final String TEMP_PATH = "temp";
    private final String path;
    private boolean filterBeforeCopyRemote;
    private long lastSnapshotIndex;
    private final RaftOptions raftOptions;

    public LocalSnapshotStorage(String path, RaftOptions raftOptions) {
        this.path = path;
        this.raftOptions = raftOptions;
    }

    @Override
    public SnapshotReader open() {
        return null;
    }

    @Override
    public boolean init(Void opts) {
        final File dir = new File(this.path);

        try {
            FileUtils.forceMkdir(dir);
        } catch (final IOException e) {
            LOG.error("Fail to create directory {}.", this.path);
            return false;
        }

        if (!this.filterBeforeCopyRemote) {
            final String tempSnapshotPath = this.path + File.separator + TEMP_PATH;
            final File tempFile = new File(tempSnapshotPath);
            if (tempFile.exists()) {
                try {
                    FileUtils.forceDelete(tempFile);
                } catch (final IOException e) {
                    LOG.error("Fail to delete temp snapshot path {}.", tempSnapshotPath);
                    return false;
                }
            }
        }

        final List<Long> snapshots = new ArrayList<>();
        final File[] oldFiles = dir.listFiles();
        if (oldFiles != null) {
            for (final File sFile : oldFiles) {
                final String name = sFile.getName();
                if (!name.startsWith(Snapshot.RAFT_SNAPSHOT_PREFIX)) {
                    continue;
                }
                final long index = Long.parseLong(name.substring(Snapshot.RAFT_SNAPSHOT_PREFIX.length()));
                snapshots.add(index);
            }
        }

        if (!snapshots.isEmpty()) {
            Collections.sort(snapshots);
            final int snapshotCount = snapshots.size();

            for (int i = 0; i < snapshotCount - 1; i++) {
                final long index = snapshots.get(i);
                final String snapshotPath = getSnapshotPath(index);
                if (!destroySnapshot(snapshotPath)) {
                    return false;
                }
            }
            this.lastSnapshotIndex = snapshots.get(snapshotCount - 1);
//            ref(this.lastSnapshotIndex);
        }
        return true;
    }

    private boolean destroySnapshot(final String path) {
        LOG.info("Deleting snapshot {}.", path);
        final File file = new File(path);
        try {
            FileUtils.deleteDirectory(file);
            return true;
        } catch (final IOException e) {
            LOG.error("Fail to destroy snapshot {}.", path);
            return false;
        }
    }

    private String getSnapshotPath(final long index) {
        return this.path + File.separator + Snapshot.RAFT_SNAPSHOT_PREFIX + index;
    }

    @Override
    public SnapshotWriter create() {
        return create(true);
    }

    public SnapshotWriter create(final boolean fromEmpty) {
        LocalSnapshotWriter writer;

        final String snapshotPath = this.path + File.separator + TEMP_PATH;
        if (new File(snapshotPath).exists() && fromEmpty) {
            if (!destroySnapshot(snapshotPath)) {
                return null;
            }
        }
        writer = new LocalSnapshotWriter(snapshotPath, this, this.raftOptions);
        if (!writer.init(null)) {
            LOG.error("Fail to init snapshot writer.");
            return null;
        }
        LOG.info("ceate local snaphot writer!");
        return writer;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getPath() {
        return null;
    }
}
