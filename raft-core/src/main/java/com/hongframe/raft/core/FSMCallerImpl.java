package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.StateMachine;
import com.hongframe.raft.option.FSMCallerOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 20:04
 */
public class FSMCallerImpl implements FSMCaller {

    private StateMachine stateMachine;

    @Override
    public boolean onCommitted(long committedIndex) {
        return false;
    }

    @Override
    public boolean init(FSMCallerOptions opts) {
        this.stateMachine = opts.getFsm();
        return true;
    }

    @Override
    public void shutdown() {

    }
}
