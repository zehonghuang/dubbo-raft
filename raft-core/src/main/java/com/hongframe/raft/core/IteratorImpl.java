package com.hongframe.raft.core;

import com.hongframe.raft.StateMachine;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.storage.LogManager;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:06
 */
public class IteratorImpl {

    private final StateMachine stateMachine;
    private final LogManager logManager;
    private final List<Callback> callbacks;
    private final long firstIndex;
    private final long committedIndex;
    private long currIndex;
    private LogEntry currEntry;

    public IteratorImpl(StateMachine stateMachine, LogManager logManager, List<Callback> callbacks, long firstIndex, long committedIndex) {
        this.stateMachine = stateMachine;
        this.logManager = logManager;
        this.callbacks = callbacks;
        this.firstIndex = firstIndex;
        this.committedIndex = committedIndex;
        next();
    }

    public void next() {

    }

    public LogEntry entry() {
        return currEntry;
    }

    public long getIndex() {
        return this.currIndex;
    }
}
