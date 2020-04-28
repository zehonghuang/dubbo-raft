package com.hongframe.raft.storage.impl;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.option.LogManagerOptions;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.util.ArrayDeque;
import com.hongframe.raft.util.DisruptorBuilder;
import com.hongframe.raft.util.NamedThreadFactory;
import com.hongframe.raft.util.SegmentList;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 19:16
 */
public class LogManagerImpl implements LogManager {

    private RaftOptions raftOptions;
    private LogStorage logStorage;
    private ConfigurationManager configManager;
    private FSMCaller caller;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = this.lock.writeLock();
    private final Lock readLock = this.lock.readLock();
    private final SegmentList<LogEntry> logsInMemory = new SegmentList<>();
    private Disruptor<FlushDoneCallbackEvent> disruptor;
    private RingBuffer<FlushDoneCallbackEvent> diskQueue;

    private LogId diskId = new LogId(0, 0);
    private LogId appliedId = new LogId(0, 0);
    private volatile long firstLogIndex;
    private volatile long lastLogIndex;

    private static class FlushDoneCallbackEvent {
        FlushDoneCallback callback;

        //TODO 增加 event type ？
        void reset() {
            callback = null;
        }
    }

    private static class FlushDoneCallbackEventFactory implements EventFactory<FlushDoneCallbackEvent> {

        @Override
        public FlushDoneCallbackEvent newInstance() {
            return new FlushDoneCallbackEvent();
        }
    }

    private static class FlushDoneCallbackEventHandler implements EventHandler<FlushDoneCallbackEvent> {
        @Override
        public void onEvent(FlushDoneCallbackEvent event, long sequence, boolean endOfBatch) throws Exception {
            //TODO FlushDoneCallbackEventHandler
        }
    }

    @Override
    public boolean init(LogManagerOptions opts) {
        this.writeLock.lock();
        try {
            this.raftOptions = opts.getRaftOptions();
            this.logStorage = opts.getLogStorage();
            this.configManager = opts.getConfigurationManager();
            this.caller = opts.getCaller();

            this.firstLogIndex = this.logStorage.getFirstLogIndex();
            this.lastLogIndex = this.logStorage.getLastLogIndex();

            this.disruptor = DisruptorBuilder.<FlushDoneCallbackEvent>newInstance()
                    .setEventFactory(new FlushDoneCallbackEventFactory())
                    .setRingBufferSize(opts.getDisruptorBufferSize())
                    .setThreadFactory(new NamedThreadFactory("Dubbo-Raft-LogManager-Disruptor-", true))
                    .setProducerType(ProducerType.MULTI)
                    .setWaitStrategy(new TimeoutBlockingWaitStrategy(
                            this.raftOptions.getDisruptorPublishEventWaitTimeoutSecs(), TimeUnit.SECONDS))
                    .build();
            this.disruptor.handleEventsWith(new FlushDoneCallbackEventHandler());
            this.diskQueue = this.disruptor.start();
            return true;
        } finally {
            this.writeLock.unlock();
        }
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
    public void appendEntries(List<LogEntry> entries, FlushDoneCallback callback) {
        this.writeLock.lock();
        try {
            if (!entries.isEmpty() && !checkAndResolveConflict(entries)) {
                return;
            }
            for (final LogEntry entry : entries) {
                if (entry.getType() == EntryType.ENTRY_TYPE_CONFIGURATION) {
                    //TODO ENTRY_TYPE_CONFIGURATION
                }
            }
            if (!entries.isEmpty()) {
                this.logsInMemory.addAll(entries);
                callback.setFirstLogIndex(entries.get(0).getId().getIndex());
            }
            callback.setEntries(entries);
            //TODO flush disk
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public LogEntry getEntry(long index) {
        return null;
    }

    private boolean checkAndResolveConflict(final List<LogEntry> entries) {
        final LogEntry first = ArrayDeque.peekFirst(entries);
        if (first.getId().getIndex() == 0) {
            for (LogEntry entry : entries) {
                entry.getId().setIndex(++this.lastLogIndex);
            }
            return true;
        }
        return false;
    }

    @Override
    public void shutdown() {

    }
}
