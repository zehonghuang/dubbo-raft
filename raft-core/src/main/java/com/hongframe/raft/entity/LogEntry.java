package com.hongframe.raft.entity;

import java.nio.ByteBuffer;
import java.util.List;

public class LogEntry {

    private EntryType type;
    private LogId id = new LogId(0, 0);
    private List<PeerId> peers;
    private List<PeerId> oldPeers;
    private ByteBuffer data;

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public LogId getId() {
        return id;
    }

    public void setId(LogId id) {
        this.id = id;
    }

    public List<PeerId> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerId> peers) {
        this.peers = peers;
    }

    public List<PeerId> getOldPeers() {
        return oldPeers;
    }

    public void setOldPeers(List<PeerId> oldPeers) {
        this.oldPeers = oldPeers;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
