package com.hongframe.raft.storage.impl;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.Status;
import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.option.LogManagerOptions;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.util.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 19:16
 */
public class LogManagerImpl implements LogManager {

    private static final Logger LOG = LoggerFactory.getLogger(LogManagerImpl.class);

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

    private final Map<Long, WaitMeta> waitMap = new HashMap<>();
    private long nextWaitId;
    private LogId diskId = new LogId(0, 0);
    private LogId appliedId = new LogId(0, 0);
    private volatile long firstLogIndex;
    private volatile long lastLogIndex;

    private static class FlushDoneCallbackEvent {
        FlushDoneCallback callback;
        EventType type;

        //TODO 增加 event type ？
        void reset() {
            callback = null;
        }
    }

    private enum EventType {
        OTHER,
        LAST_LOG_ID;
    }

    public void setDiskId(LogId diskId) {
        if (diskId == null) {
            return;
        }
        this.writeLock.lock();
        try {
            if (diskId.compareTo(this.diskId) <= 0) {
                return;
            }
            this.diskId = diskId;
            clearMemoryLogs(this.diskId.compareTo(this.appliedId) <= 0 ? this.diskId : this.appliedId);
        } finally {
            this.writeLock.unlock();
        }

    }


    private void clearMemoryLogs(final LogId id) {
        this.writeLock.lock();
        try {
            this.logsInMemory.removeFromFirstWhen(entry -> entry.getId().compareTo(id) <= 0);
        } finally {
            this.writeLock.unlock();
        }
    }

    private static class FlushDoneCallbackEventFactory implements EventFactory<FlushDoneCallbackEvent> {

        @Override
        public FlushDoneCallbackEvent newInstance() {
            return new FlushDoneCallbackEvent();
        }
    }

    private class FlushDoneCallbackEventHandler implements EventHandler<FlushDoneCallbackEvent> {
        LogId lastId = LogManagerImpl.this.diskId; //TODO flush last log index
        List<FlushDoneCallback> storage = new ArrayList<>(256);
        AppendBatcher batcher = new AppendBatcher(this.storage, 256, LogManagerImpl.this.diskId);

        @Override
        public void onEvent(FlushDoneCallbackEvent event, long sequence, boolean endOfBatch) throws Exception {
            //TODO FlushDoneCallbackEventHandler
            FlushDoneCallback callback = event.callback;
            if (callback.getEntries() != null && !callback.getEntries().isEmpty()) {
                this.batcher.append(callback);
            } else {
                this.lastId = this.batcher.flush();
                switch (event.type) {
                    case LAST_LOG_ID:
                        ((LastLogIdCallback) event.callback).setLastLogId(this.lastId);
                        break;
                }
                event.callback.run(Status.OK());
            }
            if (endOfBatch) {
                this.lastId = this.batcher.flush();
                setDiskId(this.lastId);
            }
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
    public long getFirstLogIndex() {
        this.readLock.lock();
        try {
            return this.firstLogIndex;
        } finally {
            this.readLock.unlock();
        }
    }

    private class LastLogIdCallback extends FlushDoneCallback {
        private LogId lastLogId;
        private CountDownLatch latch;

        public LastLogIdCallback() {
            super(null);
            this.latch = new CountDownLatch(1);
        }

        public LogId getLastLogId() throws InterruptedException {
            this.latch.await();
            return lastLogId;
        }

        public void setLastLogId(LogId lastLogId) {
            this.lastLogId = lastLogId;
        }

        @Override
        public void run(Status status) {
            this.latch.countDown();
        }
    }

    @Override
    public long getLastLogIndex() {
        return getLastLogIndex(false);
    }

    @Override
    public long getLastLogIndex(boolean isFlush) {
        return getLastLogId(isFlush).getIndex();
    }

    @Override
    public LogId getLastLogId(boolean isFlush) {
        this.readLock.lock();
        try {
            if (!isFlush) {
                return new LogId(this.lastLogIndex, getTerm(this.lastLogIndex));
            } else {
                LastLogIdCallback callback = new LastLogIdCallback();
                EventTranslator<FlushDoneCallbackEvent> translator = (event, sequence) -> {
                    event.reset();
                    event.type = EventType.LAST_LOG_ID;
                    event.callback = callback;
                };
                this.diskQueue.tryPublishEvent(translator);
                try {
                    return callback.getLastLogId();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setAppliedId(LogId appliedId) {
        LogId clearId;
        this.writeLock.lock();
        try {
            if (appliedId.compareTo(this.appliedId) < 0) {
                return;
            }
            this.appliedId = appliedId.copy();
            clearId = this.diskId.compareTo(this.appliedId) <= 0 ? this.diskId : this.appliedId;
        } finally {
            this.writeLock.unlock();
        }

        if (clearId != null) {
            clearMemoryLogs(clearId);
        }
    }

    @Override
    public long getTerm(long index) {
        if (index == 0) {
            return 0;
        }
        LogEntry entry;
        this.writeLock.lock();
        try {
            entry = getEntryFromMemory(index);
            if (entry != null) {
                return entry.getId().getTerm();
            }
        } finally {
            this.writeLock.unlock();
        }
        entry = this.logStorage.getEntry(index);
        if (entry != null) {
            return entry.getId().getTerm();
        }
        return 0;
    }

    @Override
    public void appendEntries(List<LogEntry> entries, FlushDoneCallback callback) {
        LOG.info("executeTasks -> appendEntries");
        boolean doUnlock = true;
        this.writeLock.lock();
        try {
            if (!entries.isEmpty() && !checkAndResolveConflict(entries, callback)) {
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

            final EventTranslator<FlushDoneCallbackEvent> translator = (event, sequence) -> {
                event.reset();
                event.callback = callback;
                event.type = EventType.OTHER;
            };
            while (true) {
                if (this.diskQueue.tryPublishEvent(translator)) {
                    break;
                } else {
                    Thread.yield();
                }
            }
            doUnlock = false;
            wakeupAllWaiter();
        } finally {
            if(doUnlock) {
                this.writeLock.unlock();
            }
        }
    }

    private boolean wakeupAllWaiter() {
        if(this.waitMap.isEmpty()) {
            this.writeLock.unlock();
            return false;
        }
        final List<WaitMeta> wms = new ArrayList<>(this.waitMap.values());
        this.waitMap.clear();
        this.writeLock.unlock();

        final int waiterCount = wms.size();
        for (int i = 0; i < waiterCount; i++) {
            final WaitMeta wm = wms.get(i);
            Utils.runInThread(() -> runOnNewLog(wm));
        }
        return true;
    }

    @Override
    public LogEntry getEntry(long index) {
        this.readLock.lock();
        try {
            if (index < this.firstLogIndex || index > this.lastLogIndex) {
                return null;
            }
            LogEntry entry = getEntryFromMemory(index);
            if (entry != null) {
                return entry;
            }
        } finally {
            this.readLock.unlock();
        }
        LogEntry entry = this.logStorage.getEntry(index);
        if (entry == null) {
            //TODO error
        }
        return entry;
    }

    private LogEntry getEntryFromMemory(long index) {
        if (!logsInMemory.isEmpty()) {
            if (index <= this.lastLogIndex && index >= this.firstLogIndex) {
                return logsInMemory.get((int) (index - this.firstLogIndex));
            }
        }
        return null;
    }

    private class AppendBatcher {
        List<FlushDoneCallback> callbacks;
        int cap;
        int size;
        int bufferSize;
        List<LogEntry> entries = new ArrayList<>();
        LogId lastId;

        public AppendBatcher(List<FlushDoneCallback> callbacks, int cap, LogId lastId) {
            this.callbacks = callbacks;
            this.cap = cap;
            this.lastId = lastId;
        }

        LogId flush() {
            if (this.size > 0) {
                LOG.info("flush first: {}, lastId:{}", this.entries.get(0).getId().getIndex(), this.entries.get(0).getId().getIndex() + this.entries.size() - 1);
                this.lastId = appendToStorage(this.entries);
                for (FlushDoneCallback callback : callbacks) {
                    callback.getEntries().clear();
                    callback.run(Status.OK());
                }
                this.entries.clear();
                this.callbacks.clear();
            }
            this.bufferSize = 0;
            this.size = 0;
            this.cap = 0;
            return this.lastId;
        }

        LogId appendToStorage(List<LogEntry> entries) {
            LogId lastId = null;
            final int entriesCount = entries.size();
            final int nAppent = LogManagerImpl.this.logStorage.appendEntries(entries);
            if (entriesCount != nAppent) {
                // TODO error
            }
            if (nAppent > 0) {
                lastId = entries.get(nAppent - 1).getId();
            }
            entries.clear();
            return lastId;
        }

        void append(final FlushDoneCallback callback) {
            if (this.size == this.cap || this.bufferSize >= LogManagerImpl.this.raftOptions.getMaxAppendBufferSize()) {
                flush();
            }
            this.callbacks.add(callback);
            this.size++;
            this.entries.addAll(callback.getEntries());

            for (final LogEntry entry : callback.getEntries()) {
                this.bufferSize += entry.getData() != null ? entry.getData().remaining() : 0;
            }
        }
    }

    private boolean checkAndResolveConflict(final List<LogEntry> entries, FlushDoneCallback doneCallback) {
        final LogEntry first = ArrayDeque.peekFirst(entries);
        if (first.getId().getIndex() == 0) {
            for (LogEntry entry : entries) {
                entry.getId().setIndex(++this.lastLogIndex);
                LOG.info("new entry log id: {}", entry.getId());
            }
            return true;
        } else {
            if (first.getId().getIndex() > this.lastLogIndex + 1) {
                Utils.runInThread(() -> {
                    doneCallback.run(new Status(10001, ""));
                });
                return false;
            }
            long appliedIndex = this.appliedId.getIndex();
            LogEntry lastLogEntry = ArrayDeque.peekLast(entries);
            if (lastLogEntry.getId().getIndex() <= appliedIndex) {
                return false;
            }

            if (first.getId().getIndex() == this.lastLogIndex + 1) {
                this.lastLogIndex = lastLogEntry.getId().getIndex();
            } else {

            }
        }
        return true;
    }

    private static class WaitMeta {
        NewLogNotification notify;
        int errorCode;
        Object arg;

        public WaitMeta(final NewLogNotification notify, final Object arg, final int errorCode) {
            super();
            this.notify = notify;
            this.arg = arg;
            this.errorCode = errorCode;
        }

    }

    @Override
    public long wait(long expectedLastLogIndex, NewLogNotification notify, Object arg) {
        final WaitMeta wm = new WaitMeta(notify, arg, 0);
        this.writeLock.lock();
        try {
            if (expectedLastLogIndex != this.lastLogIndex) {
                Utils.runInThread(() -> runOnNewLog(wm));
                return 0;
            }
            if (this.nextWaitId == 0) {
                this.nextWaitId++;
            }
            long waitid = this.nextWaitId++;
            this.waitMap.put(waitid, wm);
            return waitid;
        } finally {
            this.writeLock.unlock();
        }
    }

    void runOnNewLog(final WaitMeta wm) {
        wm.notify.onNewLog(wm.arg, wm.errorCode);
    }

    @Override
    public void shutdown() {

    }
}
