package com.hongframe.raft.entity;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:20
 */
public class SnapshotMeta implements Serializable {

    private long lastIncludedIndex;
    private long lastIncludedTerm;
    private String peers;
    private String oldPeers;

    public long getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    public void setLastIncludedIndex(long lastIncludedIndex) {
        this.lastIncludedIndex = lastIncludedIndex;
    }

    public long getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    public void setLastIncludedTerm(long lastIncludedTerm) {
        this.lastIncludedTerm = lastIncludedTerm;
    }

    public String getPeers() {
        return peers;
    }

    public void setPeers(String peers) {
        this.peers = peers;
    }

    public String getOldPeers() {
        return oldPeers;
    }

    public void setOldPeers(String oldPeers) {
        this.oldPeers = oldPeers;
    }

    @Override
    public String toString() {
        return "SnapshotMeta{" +
                "lastIncludedIndex=" + lastIncludedIndex +
                ", lastIncludedTerm=" + lastIncludedTerm +
                ", peers='" + peers + '\'' +
                ", oldPeers='" + oldPeers + '\'' +
                '}';
    }
}
