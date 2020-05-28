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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-21 19:43
 */
public class LocalSnapshotStorage extends SnapshotStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotStorage.class);

    private static final String TEMP_PATH = "temp";
    /**
     * 文件打开计数器
     */
    private final ConcurrentMap<Long, AtomicInteger> refMap = new ConcurrentHashMap<>();
    private final String path;
    private boolean filterBeforeCopyRemote;
    private long lastSnapshotIndex;
    private final Lock lock;
    private final RaftOptions raftOptions;

    public LocalSnapshotStorage(String path, RaftOptions raftOptions) {
        this.path = path;
        this.raftOptions = raftOptions;
        this.lock = new ReentrantLock();
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
            ref(this.lastSnapshotIndex);
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

    void ref(final long index) {
        final AtomicInteger refs = getRefs(index);
        refs.incrementAndGet();
    }

    AtomicInteger getRefs(final long index) {
        AtomicInteger refs = this.refMap.get(index);
        if (refs == null) {
            refs = new AtomicInteger(0);
            final AtomicInteger eRefs = this.refMap.putIfAbsent(index, refs);
            if (eRefs != null) {
                refs = eRefs;
            }
        }
        return refs;
    }

    void unref(final long index) {
        final AtomicInteger refs = getRefs(index);
        if (refs.decrementAndGet() == 0) {
            if (this.refMap.remove(index, refs)) {
                destroySnapshot(getSnapshotPath(index));
            }
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
    public SnapshotReader open() {
        long lsIndex = 0;
        this.lock.lock();
        try {
            if (this.lastSnapshotIndex != 0) {
                lsIndex = this.lastSnapshotIndex;
                ref(lsIndex);
            }
        } finally {
            this.lock.unlock();
        }
        if (lsIndex == 0) {
            LOG.warn("No data for snapshot reader {}.", this.path);
            return null;
        }
        final String snapshotPath = getSnapshotPath(lsIndex);
        SnapshotReader reader = new LocalSnapshotReader(snapshotPath, this, this.raftOptions);
        if (!reader.init(null)) {
            unref(lsIndex);
            return null;
        }
        return reader;
    }

    @Override
    public void close(LocalSnapshotWriter writer, boolean keepDataOnError) {
        int ret = writer.getCode();

        try {
            if (ret != 0) {
                return;
            }
            try {
                if (!writer.sync()) {
                    return;
                }
            } catch (IOException e) {
                LOG.error("Fail to sync writer {}.", writer.getPath());
                ret = 10001;
                return;
            }

            final long oldIndex = getLastSnapshotIndex();
            final long newIndex = writer.getSnapshotIndex();
            if (oldIndex == newIndex) {
                ret = 10001;
                return;
            }

            final String tempPath = this.path + File.separator + TEMP_PATH;
            final String newPath = getSnapshotPath(newIndex);
            if (!destroySnapshot(newPath)) {
                LOG.warn("Delete new snapshot path failed, path is {}.", newPath);
                ret = 10001;
                return;
            }
            LOG.info("Renaming {} to {}.", tempPath, newPath);
            if (!new File(tempPath).renameTo(new File(newPath))) {
                LOG.error("Renamed temp snapshot failed, from path {} to path {}.", tempPath, newPath);
                ret = 10001;
                return;
            }
            ref(newIndex);
            this.lock.lock();
            try {
                this.lastSnapshotIndex = newIndex;
            } finally {
                this.lock.unlock();
            }
            unref(oldIndex);//释放旧快照文件
        } finally {
            if (ret != 0 && !keepDataOnError) {
                //TODO error
                destroySnapshot(writer.getPath());
            }
        }

    }

    public long getLastSnapshotIndex() {
        this.lock.lock();
        try {
            return this.lastSnapshotIndex;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getPath() {
        return null;
    }
}
