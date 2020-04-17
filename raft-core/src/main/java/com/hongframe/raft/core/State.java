package com.hongframe.raft.core;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 13:55
 */
public enum State {
    STATE_LEADER,
    STATE_CANDIDATE,
    STATE_FOLLOWER,
    STATE_ERROR;

    public boolean isActive() {
        return this.ordinal() < STATE_ERROR.ordinal();
    }
}
