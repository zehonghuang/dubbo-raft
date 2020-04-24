package com.hongframe.raft.entity;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface Message extends java.io.Serializable {

    default String seviceName() {
        return null;
    }

    default String method() {
        return null;
    }

    default String getName() {
        return null;
    }

    default String getPeerId() {
        return null;
    }

    default String getGroupId() {
        return null;
    }

}
