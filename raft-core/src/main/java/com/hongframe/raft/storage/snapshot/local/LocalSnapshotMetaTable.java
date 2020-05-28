package com.hongframe.raft.storage.snapshot.local;

import com.google.protobuf.LazyStringList;
import com.google.protobuf.ProtocolStringList;
import com.hongframe.raft.entity.LocalFileMetaOutter;
import com.hongframe.raft.entity.LocalFileMetaOutter.*;
import com.hongframe.raft.entity.SnapshotMeta;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.snapshot.io.ProtoBufFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-23 01:26
 */
public class LocalSnapshotMetaTable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnapshotMetaTable.class);

    private final Map<String, LocalFileMeta> fileMap;
    private final RaftOptions raftOptions;
    private SnapshotMeta meta;

    public LocalSnapshotMetaTable(RaftOptions raftOptions) {
        this.fileMap = new HashMap<>();
        this.raftOptions = raftOptions;
    }

    public boolean addFile(final String fileName, final LocalFileMeta meta) {
        return this.fileMap.putIfAbsent(fileName, meta) == null;
    }

    public boolean removeFile(final String fileName) {
        return this.fileMap.remove(fileName) != null;
    }

    public boolean saveToFile(String path) throws IOException {
        LocalSnapshotPbMeta.Builder pbMeta = LocalSnapshotPbMeta.newBuilder();
        if (this.meta != null) {
            LocalFileMetaOutter.SnapshotMeta.Builder snMeta = LocalFileMetaOutter.SnapshotMeta.newBuilder();
            String[] peers = split(this.meta.getPeers());
            for(int i = 0; i < peers.length; i++) {
                snMeta.setPeers(i, peers[i]);
            }
            String[] oldPeers = split(this.meta.getOldPeers());
            for(int i = 0; i < oldPeers.length; i++) {
                snMeta.setOldPeers(i, oldPeers[i]);
            }
            snMeta.setLastIncludedIndex(this.meta.getLastIncludedIndex());
            snMeta.setLastIncludedTerm(this.meta.getLastIncludedTerm());
            pbMeta.setMeta(snMeta);
        }
        for (Map.Entry<String, LocalFileMeta> entry : this.fileMap.entrySet()) {
            LocalSnapshotPbMeta.File f = LocalSnapshotPbMeta.File.newBuilder().setName(entry.getKey()).setMeta(entry.getValue()).build();
            pbMeta.addFiles(f);
        }
        ProtoBufFile pbFile = new ProtoBufFile(path);
        return pbFile.save(pbMeta.build(), this.raftOptions.isSyncMeta());
    }

    public boolean loadFromFile(String path) throws IOException {
        ProtoBufFile pbFile = new ProtoBufFile(path);
        LocalSnapshotPbMeta pbMeta = pbFile.load();
        if (pbMeta == null) {
            LOG.error("Fail to load meta from {}.", path);
            return false;
        }
        return loadFromPbMeta(null);
    }

    private boolean loadFromPbMeta(final LocalSnapshotPbMeta pbMeta) {
        if (pbMeta.hasMeta()) {
            this.meta = new SnapshotMeta();
            this.meta.setLastIncludedTerm(pbMeta.getMeta().getLastIncludedTerm());
            this.meta.setLastIncludedIndex(pbMeta.getMeta().getLastIncludedIndex());
            this.meta.setPeers(lazyStringList2String(pbMeta.getMeta().getPeersList()));
            this.meta.setOldPeers(lazyStringList2String(pbMeta.getMeta().getOldPeersList()));
        } else {
            this.meta = null;
        }
        this.fileMap.clear();
        for (final LocalSnapshotPbMeta.File f : pbMeta.getFilesList()) {
            this.fileMap.put(f.getName(), f.getMeta());
        }
        return true;
    }

    private String lazyStringList2String(ProtocolStringList list) {
        StringBuilder peers = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            peers.append(list.get(i));
            if (i != list.size() - 1) {
                peers.append(",");
            }
        }
        return peers.toString();
    }

    public LocalFileMeta getFileMeta(String fileName) {
        return this.fileMap.get(fileName);
    }

    public void setMeta(SnapshotMeta meta) {
        this.meta = meta;
    }

    public SnapshotMeta getMeta() {
        return this.meta;
    }

    public boolean hasMeta() {
        return this.meta != null;
    }

    private String[] split(String s) {
        return s.split(",");
    }
}
