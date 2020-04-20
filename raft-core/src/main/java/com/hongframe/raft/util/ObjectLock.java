package com.hongframe.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ObjectLock<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLock.class);

    private final T data;
    private final CountDownLatch nonReentrant = new CountDownLatch(0) {
        private Thread owner;

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            boolean islock;
            if (islock = super.await(timeout, unit)) {
                this.owner = Thread.currentThread();
            }
            return islock;
        }

        @Override
        public void countDown() {
            if (this.owner == Thread.currentThread()) {
                super.countDown();
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
            while (!nonReentrant.await(10, TimeUnit.MILLISECONDS)) {
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
        this.nonReentrant.countDown();
    }


}
