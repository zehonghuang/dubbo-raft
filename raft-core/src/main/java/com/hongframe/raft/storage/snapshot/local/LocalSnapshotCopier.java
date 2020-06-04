package com.hongframe.raft.storage.snapshot.local;

import com.hongframe.raft.entity.LocalFileMeta;
import com.hongframe.raft.entity.LocalFileMetaOutter;
import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.storage.snapshot.Snapshot;
import com.hongframe.raft.storage.snapshot.SnapshotCopier;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.remote.RemoteFileCopier;
import com.hongframe.raft.storage.snapshot.remote.Session;
import com.hongframe.raft.util.ByteBufferCollector;
import com.hongframe.raft.util.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        this.lock.lock();
        try {
            if (this.cancelled) {
                return;
            }
            if(isOk()) {
                setError(10001, "Cancel copier!");
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

    private void startCopy() {
        try {
            loadMetaTable();
            if (!isOk()) {
                return;
            }
            filter();
            if (!isOk()) {
                return;
            }
            final Set<String> files = this.remoteSnapshot.listFiles();
            for (final String file : files) {
                copyFile(file);
            }
            if (this.writer != null) {
                this.writer.close();
                this.writer = null;
            }
            if (isOk()) {
                this.reader = (LocalSnapshotReader) this.storage.open();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    /**
     * TODO 需要搞明白
     *
     * @param writer
     * @param reader
     * @return
     */
    boolean filterBeforeCopy(final LocalSnapshotWriter writer, final LocalSnapshotReader reader) throws IOException {
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
            if (reader == null) {
                continue;
            }
            if ((localMeta = reader.getFileMeta(filename)) == null) {
                continue;
            }
            if (!localMeta.hasChecksum() || !localMeta.getChecksum().equals(remoteMeta.getChecksum())) {
                continue;
            }
            if (localMeta.getSource() == LocalFileMetaOutter.FileSource.FILE_SOURCE_LOCAL.getNumber()) {
                //TODO
                String sourcePath = reader.getPath() + File.separator + filename;
                String destPath = writer.getPath() + File.separator + filename;
                FileUtils.deleteQuietly(new File(destPath));
                try {
                    Files.createLink(Paths.get(destPath), Paths.get(sourcePath));
                } catch (final IOException e) {
                    LOG.error("Fail to link {} to {}", sourcePath, destPath, e);
                    continue;
                }
                if (!toRemove.isEmpty() && toRemove.peekLast().equals(filename)) {
                    toRemove.pollLast();
                }
            }
            writer.addFile(filename, localMeta);
        }
        if (!writer.sync()) {
            LOG.error("Fail to sync writer on path={}", writer.getPath());
            return false;
        }
        for (final String fileName : toRemove) {
            final String removePath = writer.getPath() + File.separator + fileName;
            FileUtils.deleteQuietly(new File(removePath));
            LOG.info("Deleted file: {}", removePath);
        }
        return true;//TODO filterBeforeCopy
    }

    void copyFile(final String fileName) throws InterruptedException, IOException {
        if (this.writer.getFileMeta(fileName) != null) {
            LOG.info("Skipped downloading {}", fileName);
            return;
        }

        final String filePath = this.writer.getPath() + File.separator + fileName;
        final Path subPath = Paths.get(filePath);
        if (!subPath.equals(subPath.getParent()) && !subPath.getParent().getFileName().toString().equals(".")) {
            final File parentDir = subPath.getParent().toFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                LOG.error("Fail to create directory for {}", filePath);
                setError(10001, "Fail to create directory");
                return;
            }
        }
        final LocalFileMeta meta = this.remoteSnapshot.getFileMeta(fileName);
        Session session = null;
        try {
            this.lock.lock();
            try {
                if (this.cancelled) {
                    if (isOk()) {
                        setError(10001, "ECANCELED");
                    }
                    return;
                }
                session = this.copier.startCopyToFile(fileName, filePath, null);
                if (session == null) {
                    setError(10001, "Fail to copy %s", fileName);
                    return;
                }
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
            if (!this.writer.addFile(fileName, meta)) {
                setError(10001, "Fail to add file to writer");
                return;
            }
            if (!this.writer.sync()) {
                setError(10001, "Fail to sync writer");
            }
        } finally {
            if (session != null) {
                session.close();

            }
        }
        //TODO copyFile

    }

    @Override
    public void start() {
        this.future = Utils.runInThread(this::startCopy);
    }

    @Override
    public SnapshotReader getReader() {
        return this.reader;
    }

    @Override
    public void close() throws IOException {
        cancel();
        try {
            join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public LocalSnapshotStorage getStorage() {
        return storage;
    }

    public void setStorage(LocalSnapshotStorage storage) {
        this.storage = storage;
    }

    public void setFilterBeforeCopyRemote(boolean filterBeforeCopyRemote) {
        this.filterBeforeCopyRemote = filterBeforeCopyRemote;
    }
}
