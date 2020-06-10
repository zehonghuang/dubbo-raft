package com.hongframe.raft.slime.metadata;

import com.hongframe.raft.util.Copiable;
import com.hongframe.raft.util.Endpoint;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 09:15
 */
public class Peer implements Copiable<Peer>, Serializable {

    private long id;
    private long storeId;
    private Endpoint endpoint;

    public Peer() {
    }

    public Peer(long id, long storeId, Endpoint endpoint) {
        this.id = id;
        this.storeId = storeId;
        this.endpoint = endpoint;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Peer copy() {
        Endpoint endpoint = null;
        if (this.endpoint != null) {
            endpoint = this.endpoint.copy();
        }
        return new Peer(this.id, this.storeId, endpoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Peer peer = (Peer) o;
        return id == peer.id && storeId == peer.storeId && Objects.equals(endpoint, peer.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storeId, endpoint);
    }

    @Override
    public String toString() {
        return "Peer{" +
                "id=" + id +
                ", storeId=" + storeId +
                ", endpoint=" + endpoint +
                '}';
    }
}
