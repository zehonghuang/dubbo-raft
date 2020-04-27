package com.hongframe.raft;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 01:13
 */
public interface StateMachine {

    void onApply(Iterator iterator);

    void onShutdown();

    void onLeaderStart(Status status);

    void onLeaderStop(Status status);

}
