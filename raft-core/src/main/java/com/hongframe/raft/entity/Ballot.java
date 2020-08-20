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

    private final List<UnfoundPeerId> _newPeers = new ArrayList<>();
    private final List<UnfoundPeerId> _oldPeers = new ArrayList<>();
    private int _newQuorum, _oldQuorum;

    public boolean init(Configuration _new, Configuration _old) {
        _newPeers.clear();
        this._newQuorum = 0;
        int newIndex = 0;
        if (Objects.nonNull(_new)) {
            for (PeerId peerId : _new.getPeers()) {
                this._newPeers.add(new UnfoundPeerId(peerId, false, newIndex++));
            }
        }
        this._newQuorum = this._newPeers.size() / 2 + 1;
        if (_old == null) {
            return true;
        }

        this._oldQuorum = 0;
        int oldIndex = 0;
        if (Objects.nonNull(_old)) {
            for (PeerId peerId : _old.getPeers()) {
                this._oldPeers.add(new UnfoundPeerId(peerId, false, oldIndex++));
            }
        }
        this._oldQuorum = this._oldPeers.size() / 2 + 1;
        return true;
    }

    /**
     * TODO 新旧配置共同一致需要测试
     *
     * @param peerId
     * @param hint
     * @return
     */
    public PosHint grant(PeerId peerId, PosHint hint) {
        if (hint.pos0 < 0 || hint.pos0 >= this._newPeers.size()) {
            for (UnfoundPeerId ufp : this._newPeers) {
                if (Objects.equals(peerId, ufp.peerId)) {
                    if (!ufp.found) {
                        hint.pos0 = ufp.index;
                        ufp.found = true;
                        this._newQuorum--;
                    } else {
                        hint.pos0 = -1;
                    }
                }
            }
        }
        if (this._oldPeers.isEmpty()) {
            hint.pos1 = -1;
            return null;
        }
        if (hint.pos1 < 0 || hint.pos1 >= this._oldPeers.size()) {
            for (UnfoundPeerId ufp : this._oldPeers) {
                if (Objects.equals(peerId, ufp.peerId)) {
                    if (!ufp.found) {
                        hint.pos1 = ufp.index;
                        ufp.found = true;
                        this._oldQuorum--;
                    } else {
                        hint.pos1 = -1;
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
        return this._newQuorum <= 0 && this._oldQuorum <= 0;
    }

}
