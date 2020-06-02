package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.entity.LocalFileMeta;
import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.storage.snapshot.Snapshot;
import com.hongframe.raft.storage.snapshot.SnapshotCopier;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.remote.RemoteFileCopier;
import com.hongframe.raft.storage.snapshot.remote.Session;
import com.hongframe.raft.util.ByteBufferCollector;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 17:25
 */
public class LocalSnapshotCopier extends SnapshotCopier {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotCopier.class);

    private final Lock lock = new ReentrantLock();
    private volatile Future<?> future;
    private boolean cancelled;
    private LocalSnapshotStorage storage;
    private LocalSnapshotWriter writer;
    private volatile LocalSnapshotReader reader;
    private boolean filterBeforeCopyRemote;
    private LocalSnapshot remoteSnapshot;
    private RemoteFileCopier copier;
    private Session curSession;

    public boolean init(final String uri, final SnapshotCopierOptions opts) {
        this.copier = new RemoteFileCopier();
        this.cancelled = false;
        this.filterBeforeCopyRemote = opts.getNodeOptions().isFilterBeforeCopyRemote();
        this.remoteSnapshot = new LocalSnapshot(opts.getRaftOptions());
        return this.copier.init(uri, opts);
    }

    @Override
    public void cancel() {
        this.lock.lock();
        try {
            if (this.cancelled) {
                return;
            }
            if (isOk()) {
                setError(10001, "Cancel the copier manually.");
            }
            this.cancelled = true;
            if (this.curSession != null) {
                this.curSession.cancel();
            }
            if (this.future != null) {
                this.future.cancel(true);
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void join() throws InterruptedException {

    }

    private void startCopy() {

    }

    private void loadMetaTable() throws InterruptedException {
        final ByteBufferCollector metaBuf = ByteBufferCollector.allocate(0);
        Session session = null;
        try {
            this.lock.lock();
            try {
                if (this.cancelled) {
                    if (isOk()) {
                        setError(10000, "ECANCELED");
                    }
                    return;
                }
                session = this.copier.startCopy2IoBuffer(Snapshot.RAFT_SNAPSHOT_META_FILE, metaBuf, null);
                this.curSession = session;
            } finally {
                this.lock.unlock();
            }
            session.join();
            this.lock.lock();
            try {
                this.curSession = null;
            } finally {
                this.lock.unlock();
            }
            if (!session.status().isOk()) {
                return;
            }
            if (!this.remoteSnapshot.getMetaTable().loadFromIoBufferAsRemote(metaBuf.getBuffer())) {
                //TODO loadFromIoBufferAsRemote error
                return;
            }
            if (!this.remoteSnapshot.getMetaTable().hasMeta()) {
                throw new IllegalArgumentException("Invalid remote snapshot meta!!");
            }
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void filter() throws IOException {
        this.writer = (LocalSnapshotWriter) this.storage.create(!this.filterBeforeCopyRemote);
        if (this.writer == null) {
            setError(10001, "fail to create writer");
            return;
        }
        if (this.filterBeforeCopyRemote) {
            SnapshotReader reader = this.storage.open();
            if (!filterBeforeCopy(this.writer, (LocalSnapshotReader) reader)) {
                this.writer.setError(-1, "Fail to filter");
                this.writer.close();
                this.writer = (LocalSnapshotWriter) this.storage.create(true);
            }
            if (reader != null) {
                reader.close();
            }
            if (this.writer == null) {
                setError(10001, "Fail to create writer");
                return;
            }
        }
        this.writer.saveMeta(this.remoteSnapshot.getMetaTable().getMeta());
        if (!this.writer.sync()) {
            setError(10001, "Fail to sync snapshot writer");
        }
    }

    boolean filterBeforeCopy(final LocalSnapshotWriter writer, final LocalSnapshotReader reader) {
        final Set<String> existingFiles = writer.listFiles();
        final ArrayDeque<String> toRemove = new ArrayDeque<>();
        for (final String file : existingFiles) {
            if (this.remoteSnapshot.getFileMeta(file) == null) {
                toRemove.add(file);
                writer.removeFile(file);
            }
        }
        final Set<String> remoteFiles = this.remoteSnapshot.listFiles();
        for (String filename : remoteFiles) {
            final LocalFileMeta remoteMeta = this.remoteSnapshot.getFileMeta(filename);
            if (!remoteMeta.hasChecksum()) {
                writer.removeFile(filename);
                toRemove.add(filename);
                continue;
            }
            LocalFileMeta localMeta = writer.getFileMeta(filename);
            if (localMeta != null) {
                if (localMeta.hasChecksum() && localMeta.getChecksum().equals(remoteMeta.getChecksum())) {
                    LOG.info("Keep file={} checksum={} in {}", filename, remoteMeta.getChecksum(), writer.getPath());
                    continue;
                }
                writer.removeFile(filename);
                toRemove.add(filename);
            }
            //TODO 如果远程文件本地没有怎么办？？
            if(reader == null) {
                continue;
            }
        }

        return true;//TODO filterBeforeCopy
    }

    @Override
    public void start() {
        this.future = Utils.runInThread(this::startCopy);
    }

    @Override
    public SnapshotReader getReader() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    public LocalSnapshotStorage getStorage() {
        return storage;
    }

    public void setStorage(LocalSnapshotStorage storage) {
        this.storage = storage;
    }
}
