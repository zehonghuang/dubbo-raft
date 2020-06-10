package com.hongframe.raft.slime.metadata;

import com.hongframe.raft.slime.util.Lists;
import com.hongframe.raft.util.Copiable;

import java.io.Serializable;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 09:11
 */
public class Region implements Copiable<Region>, Serializable {

    private long id;
    private byte[] startKey;
    private byte[] endKey;
    private RegionEpoch epoch;
    private List<Peer> peers;

    public Region(long id, byte[] startKey, byte[] endKey, RegionEpoch epoch, List<Peer> peers) {
        this.id = id;
        this.startKey = startKey;
        this.endKey = endKey;
        this.epoch = epoch;
        this.peers = peers;
    }

    public Region() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getStartKey() {
        return startKey;
    }

    public void setStartKey(byte[] startKey) {
        this.startKey = startKey;
    }

    public byte[] getEndKey() {
        return endKey;
    }

    public void setEndKey(byte[] endKey) {
        this.endKey = endKey;
    }

    public RegionEpoch getEpoch() {
        return epoch;
    }

    public void setEpoch(RegionEpoch epoch) {
        this.epoch = epoch;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    @Override
    public Region copy() {
        RegionEpoch regionEpoch = null;
        if (this.epoch != null) {
            regionEpoch = this.epoch.copy();
        }
        List<Peer> peers = null;
        if (this.peers != null) {
            peers = Lists.newArrayListWithCapacity(this.peers.size());
            for (Peer peer : this.peers) {
                peers.add(peer.copy());
            }
        }
        return new Region(this.id, this.startKey, this.endKey, regionEpoch, peers);
    }
}
