package com.hongframe.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ObjectLock<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLock.class);

    private final T data;
    private final Semaphore nonReentrant = new Semaphore(1) {
        private Thread owner;

        @Override
        public boolean tryAcquire() {
            return super.tryAcquire();
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
            if(this.owner != null) {
                return false;
            }
            boolean b = super.tryAcquire(timeout, unit);
            if(b) {
                this.owner = Thread.currentThread();
            }
            return b;
        }

        @Override
        public void release() {
            if(this.owner == Thread.currentThread()) {
                super.release();
                this.owner = null;
            }
        }
    };

    private volatile boolean destroyed;

    public ObjectLock(T data) {
        this.data = data;
        this.destroyed = false;
    }

    public T getData() {
        return data;
    }

    public T lock() {
        if (this.destroyed) {
            return null;
        }
        try {
            while (!nonReentrant.tryAcquire(10, TimeUnit.MILLISECONDS)) {
                if (destroyed) {
                    return null;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return this.data;
    }

    public void unlock() {
        this.nonReentrant.release();
    }


}
