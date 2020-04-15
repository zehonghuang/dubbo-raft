package com.hongframe.raft.entity;

import com.hongframe.raft.util.Endpoint;
import com.hongframe.raft.util.Utils;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:29
 */
public class PeerId {

    private Endpoint endpoint = new Endpoint(Utils.IP_ANY, 0);

    public static PeerId emptyPeer() {
        return new PeerId();
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
}
