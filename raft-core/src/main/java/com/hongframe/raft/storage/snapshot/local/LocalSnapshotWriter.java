package com.hongframe.raft.storage.snapshot.local;

import com.google.protobuf.Message;
import com.hongframe.raft.entity.LocalFileMetaOutter.*;
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
public class LocalSnapshotWriter implements SnapshotWriter {

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
        return true;
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

    @Override
    public void shutdown() {

    }
}
