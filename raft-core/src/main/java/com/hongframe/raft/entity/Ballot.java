package com.hongframe.raft.entity;

import com.hongframe.raft.conf.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 14:18
 */
public class Ballot {

    public static final class PosHint {
        int pos0 = -1;
        int pos1 = -1;
    }

    public static class UnfoundPeerId {
        PeerId peerId;
        boolean found;
        int index;

        public UnfoundPeerId(PeerId peerId, boolean found, int index) {
            this.peerId = peerId;
            this.found = found;
            this.index = index;
        }
    }

    private final List<UnfoundPeerId> peers = new ArrayList<>();
    private int quorum;

    public boolean init(Configuration config) {
        peers.clear();
        this.quorum = 0;
        int index = 0;
        if (Objects.nonNull(config)) {
            for (PeerId peerId : config.getPeers()) {
                this.peers.add(new UnfoundPeerId(peerId, false, index++));
            }
        }
        this.quorum = this.peers.size() / 2 + 1;
        return true;
    }

    public PosHint grant(PeerId peerId, PosHint hint) {
        if(hint.pos0 < 0 || hint.pos0 >= this.peers.size()) {
            for(UnfoundPeerId ufp : this.peers) {
                if(Objects.equals(peerId, ufp.peerId)) {
                    if(!ufp.found) {
                        hint.pos0 = ufp.index;
                        ufp.found = true;
                        this.quorum--;
                    } else {
                        hint.pos0 = -1;
                    }
                }
            }
        }
        return null;
    }

    public void grant(PeerId peerId) {
        grant(peerId, new PosHint());
    }

    public boolean isGranted() {
        return this.quorum <= 0;
    }

}
