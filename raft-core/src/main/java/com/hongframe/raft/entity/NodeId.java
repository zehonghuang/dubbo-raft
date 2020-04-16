package com.hongframe.raft.entity;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 18:04
 */
public class NodeId implements java.io.Serializable {

    private final String groupId;

    private final PeerId peerId;

    private String str;

    public NodeId(String groupId, PeerId peerId) {
        this.groupId = groupId;
        this.peerId = peerId;
    }

    public String getGroupId() {
        return groupId;
    }

    public PeerId getPeerId() {
        return peerId;
    }
}
