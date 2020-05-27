package com.hongframe.raft.storage.snapshot.local;

import com.google.protobuf.Message;
import com.hongframe.raft.entity.LocalFileMetaOutter.*;
import com.hongframe.raft.entity.SnapshotMeta;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-21 21:14
 */
public class LocalSnapshotWriter extends SnapshotWriter {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotWriter.class);

    private final LocalSnapshotMetaTable metaTable;
    private final String path;
    private final LocalSnapshotStorage snapshotStorage;

    public LocalSnapshotWriter(String path, LocalSnapshotStorage snapshotStorage, RaftOptions raftOptions) {
        this.path = path;
        this.snapshotStorage = snapshotStorage;
        this.metaTable = new LocalSnapshotMetaTable(raftOptions);
    }

    @Override
    public String getPath() {
        return this.path;
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
        final String metaPath = path + File.separator + RAFT_SNAPSHOT_META_FILE;
        final File metaFile = new File(metaPath);
        try {
            if (metaFile.exists()) {
                return metaTable.loadFromFile(metaPath);
            }
        } catch (final IOException e) {
            LOG.error("Fail to load snapshot meta from {}.", metaPath);
            return false;
        }
        return true;
    }

    @Override
    public boolean saveMeta(SnapshotMeta meta) {
        this.metaTable.setMeta(meta);
        return true;
    }

    public boolean sync() throws IOException {
        return this.metaTable.saveToFile(this.path + File.separator + RAFT_SNAPSHOT_META_FILE);
    }

    @Override
    public boolean addFile(String fileName, Message fileMeta) {
        final LocalFileMeta.Builder metaBuilder = LocalFileMeta.newBuilder();
        if (fileMeta != null) {
            metaBuilder.mergeFrom(fileMeta);
        }
        final LocalFileMeta meta = metaBuilder.build();
        return this.metaTable.addFile(fileName, meta);
    }

    public long getSnapshotIndex() {
        return this.metaTable.hasMeta() ? this.metaTable.getMeta().getLastIncludedIndex() : 0;
    }


    @Override
    public void shutdown() {
        try {
            close();
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    @Override
    public void close(boolean keepDataOnError) throws IOException {
        this.snapshotStorage.close(this, keepDataOnError);
    }

    @Override
    public void close() throws IOException {
        close(false);
    }
}
