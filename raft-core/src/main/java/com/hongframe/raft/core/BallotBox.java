package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.CallbackQueue;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.entity.Ballot;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.BallotBoxOptions;
import com.hongframe.raft.util.SegmentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:20
 */
public class BallotBox implements Lifecycle<BallotBoxOptions> {

    private static final Logger LOG = LoggerFactory.getLogger(BallotBox.class);

    private FSMCaller caller;
    private CallbackQueue callbackQueue;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = this.readWriteLock.writeLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private long lastCommittedIndex = 0;
    private long pendingIndex = 0; // 小于这个index，都是已经提交了
    private final SegmentList<Ballot> pendingMetaQueue = new SegmentList<>();


    @Override
    public boolean init(BallotBoxOptions opts) {
        this.caller = opts.getCaller();
        this.callbackQueue = opts.getCallbackQueue();
        return false;
    }

    public void clearPendingTasks() {
        this.writeLock.lock();
        try {
            this.pendingMetaQueue.clear();
            this.pendingIndex = 0;
            this.callbackQueue.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean resetPendingIndex(final long newPendingIndex) {
        this.writeLock.lock();
        try {
            if (this.pendingIndex != 0 && !this.pendingMetaQueue.isEmpty()) {
                return false;
            }
            if (newPendingIndex <= this.lastCommittedIndex) {
                return false;
            }
            this.pendingIndex = newPendingIndex;
            this.callbackQueue.resetFirstIndex(newPendingIndex);
            LOG.info("reset pending: {}", this.pendingIndex);
            return true;
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean commitAt(long firstLogIndex, long lastLogIndex, PeerId peerId) {
        LOG.info("into commit at first :{}, last: {}, peerId: {}", firstLogIndex, lastLogIndex, peerId);
        this.writeLock.lock();
        long lastCommittedIndex = 0;
        try {
            if (this.pendingIndex == 0) {
                return false;// 未被初始化
            }
            if (this.pendingIndex > lastLogIndex) {
                return true;// 已经被提交
            }
            if (lastLogIndex >= this.pendingIndex + this.pendingMetaQueue.size()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            final long startAt = Math.max(this.pendingIndex, firstLogIndex);
            for (long i = startAt; i <= lastLogIndex; i++) {
                Ballot ballot = pendingMetaQueue.get((int) (i - this.pendingIndex));
                ballot.grant(peerId);
                LOG.info("peer: {}, log index: {} grant 1", peerId.toString(), i);
                if (ballot.isGranted()) {
                    lastCommittedIndex = i;
                }
            }
            if (lastCommittedIndex == 0) {
                return true;
            }
            this.pendingMetaQueue.removeFromFirst((int) (lastCommittedIndex - this.pendingIndex) + 1);
            this.pendingIndex = lastCommittedIndex + 1;
            this.lastCommittedIndex = lastCommittedIndex;
        } finally {
            this.writeLock.unlock();
        }
        this.caller.onCommitted(lastCommittedIndex);
        return true;
    }

    public boolean appendPendingTask(final Configuration conf, final Configuration oldConf, final Callback callback) {
        LOG.info("executeTasks -> appendPendingTask");
        final Ballot bl = new Ballot();
        if (!bl.init(conf)) {
            return false;
        }
        this.writeLock.lock();
        try {
            if (this.pendingIndex <= 0) {
                return false;
            }
            this.pendingMetaQueue.add(bl);
            this.callbackQueue.appendPendingClosure(callback);
            LOG.info("pendingMetaQueue last index: {}", this.pendingIndex + this.pendingMetaQueue.size() - 1);
            return true;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        clearPendingTasks();
    }
}
