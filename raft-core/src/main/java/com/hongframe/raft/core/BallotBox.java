package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.CallbackQueue;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.option.BallotBoxOptions;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:20
 */
public class BallotBox implements Lifecycle<BallotBoxOptions> {

    private FSMCaller caller;
    private CallbackQueue callbackQueue;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = this.readWriteLock.writeLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private long lastCommittedIndex = 0;



    @Override
    public boolean init(BallotBoxOptions opts) {
        return false;
    }

    public boolean resetPendingIndex(final long newPendingIndex) {
        return false;
    }
    public boolean appendPendingTask(final Configuration conf, final Configuration oldConf, final Callback callback) {
        //TODO appendPendingTask
        return false;
    }

    @Override
    public void shutdown() {

    }
}
