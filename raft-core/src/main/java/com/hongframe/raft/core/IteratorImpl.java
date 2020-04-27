package com.hongframe.raft.core;

import com.hongframe.raft.StateMachine;
import com.hongframe.raft.rpc.Callback;
import com.hongframe.raft.storage.LogManager;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:06
 */
public class IteratorImpl {

    private StateMachine stateMachine;
    private LogManager logManager;
    private List<Callback> callbacks;


}
