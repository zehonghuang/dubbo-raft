package com.hongframe.raft.callback;

import com.hongframe.raft.Status;
import com.hongframe.raft.util.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 18:26
 */
public class CallbackQueueImpl implements CallbackQueue {

    private Lock lock;
    private long firstIndex;
    private LinkedList<Callback> queue;

    public CallbackQueueImpl() {
        this.lock = new ReentrantLock();
        this.firstIndex = 0;
        this.queue = new LinkedList<>();
    }

    @Override
    public void clear() {
        List<Callback> temp;
        this.lock.lock();
        try {
            temp = this.queue;
            this.firstIndex = 0;
        } finally {
            this.lock.unlock();
        }
        final Status status = new Status(10001, "");
        Utils.runInThread(() -> {
            for (Callback callback : temp) {
                callback.run(status);
            }
        });
    }

    @Override
    public void resetFirstIndex(long firstIndex) {
        this.lock.lock();
        try {
            this.firstIndex = firstIndex;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void appendPendingClosure(Callback callback) {
        this.lock.lock();
        try {
            this.queue.add(callback);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public long popClosureUntil(long endIndex, List<Callback> callbacks) {
        this.lock.lock();
        try {
            final int queueSize = this.queue.size();
            if (queueSize == 0 || endIndex < this.firstIndex) {
                return endIndex + 1;
            }
            if (endIndex > this.firstIndex + queueSize - 1) {
                return -1;
            }
            long fi = this.firstIndex;
            for (long i = fi; i <= endIndex; i++) {
                callbacks.add(this.queue.pollFirst());
            }
            this.firstIndex = endIndex + 1;
            return fi;
        } finally {
            this.lock.unlock();
        }
    }
}
