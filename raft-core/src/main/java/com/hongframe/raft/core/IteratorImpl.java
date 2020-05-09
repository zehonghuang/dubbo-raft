package com.hongframe.raft.core;

import com.hongframe.raft.StateMachine;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.storage.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:06
 */
public class IteratorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(IteratorImpl.class);

    private final StateMachine stateMachine;
    private final LogManager logManager;
    private final List<Callback> callbacks;
    private final long firstIndex;
    private final long committedIndex;
    private final AtomicLong applyingIndex;
    private long currIndex;
    private LogEntry currEntry;


    public IteratorImpl(StateMachine stateMachine, LogManager logManager, List<Callback> callbacks, long firstIndex,
                        long committedIndex, AtomicLong applyingIndex, long lastApplyIndex) {
        this.stateMachine = stateMachine;
        this.logManager = logManager;
        this.callbacks = callbacks;
        this.firstIndex = firstIndex;
        this.committedIndex = committedIndex;
        this.applyingIndex = applyingIndex;
        this.currIndex = lastApplyIndex;
        next();
        LOG.info("Create IteratorImpl[first index: {}, committed index: {}, applying index: {}, curr index: {}, callback size: {}]",
        this.firstIndex, this.committedIndex, this.applyingIndex.get(), this.currIndex, callbacks.size());

    }

    public void next() {
        this.currEntry = null;
        if (this.currIndex <= this.committedIndex) {
            this.currIndex++;
            if (this.currIndex <= this.committedIndex) {
                this.currEntry = this.logManager.getEntry(this.currIndex);
                if (this.currEntry == null) {
                    //TODO error

                }
                this.applyingIndex.set(this.currIndex);
            }
        }
        LOG.info("IteratorImpl Next[first index: {}, committed index: {}, applying index: {}, curr index: {}, callback size: {}]",
                this.firstIndex, this.committedIndex, this.applyingIndex.get(), this.currIndex, callbacks.size());
    }
    public Callback callback() {
        if(this.currIndex < this.firstIndex) {
            return null;
        }
        return this.callbacks.get((int) (this.currIndex - this.firstIndex));
    }

    public LogEntry entry() {
        return currEntry;
    }

    public long getIndex() {
        return this.currIndex;
    }

    public boolean isGood() {
        return this.currIndex <= this.committedIndex;
    }
}
