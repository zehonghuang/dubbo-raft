package com.hongframe.raft.conf;

import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.util.Copiable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:45
 */
public class Configuration implements Iterable<PeerId>, Copiable<Configuration> {

    private List<PeerId> peers = new ArrayList<>();

    public Configuration() {
    }

    public Configuration(List<PeerId> peers) {
        this.peers = peers;
    }

    public boolean addPeer(final PeerId peer) {
        return this.peers.add(peer);
    }

    public List<PeerId> getPeers() {
        return this.peers;
    }

    public void reset() {
        this.peers.clear();
    }

    @Override
    public Configuration copy() {
        return new Configuration(this.peers);
    }

    @Override
    public Iterator<PeerId> iterator() {
        return peers.iterator();
    }

    public boolean parse(String conf) {
        if(StringUtils.isBlank(conf)) {
            return false;
        }
        reset();
        final String[] peerStrs = StringUtils.split(conf, ',');
        for(String peerStr : peerStrs) {
            final PeerId peer = new PeerId();
            if (peer.parse(peerStr)) {
                addPeer(peer);
            } else {

            }
        }
        return true;
    }
}
