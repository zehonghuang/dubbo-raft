package com.hongframe.raft.storage.impl;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.option.LogManagerOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.LogStorage;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 19:16
 */
public class LogManagerImpl implements LogManager {

    private LogStorage logStorage;
    private ConfigurationManager configManager;
    private FSMCaller caller;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = this.lock.writeLock();
    private final Lock readLock = this.lock.readLock();

    @Override
    public boolean init(LogManagerOptions opts) {
        this.logStorage = opts.getLogStorage();
        this.configManager = opts.getConfigurationManager();
        this.caller = opts.getCaller();
        return true;
    }

    @Override
    public long getLastLogIndex() {
        return 0;
    }

    @Override
    public long getLastLogIndex(boolean isFlush) {
        return 0;
    }

    @Override
    public LogId getLastLogId(boolean isFlush) {
        return new LogId();
    }

    @Override
    public long getTerm(long index) {
        return 0;
    }

    @Override
    public void appendEntries(List<LogEntry> entries, Callback callback) {

    }

    @Override
    public void shutdown() {

    }
}
