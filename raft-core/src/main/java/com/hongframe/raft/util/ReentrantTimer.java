package com.hongframe.raft.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 23:57
 */
public abstract class ReentrantTimer {

    public static final Logger LOG = LoggerFactory.getLogger(ReentrantTimer.class);

    private final Lock lock = new ReentrantLock();
    private final Timer timer;
    private Timeout timeout;
    private final String name;
    private volatile int timeoutMs;
    private volatile boolean running;
    private volatile boolean stop;
    private volatile boolean destroyed;
    private volatile boolean invoking;


    public ReentrantTimer(String name, int timeoutMs) {
        this(name, new HashedWheelTimer(new NamedThreadFactory(name, false), 1l, TimeUnit.MILLISECONDS, 256), timeoutMs);
    }

    public ReentrantTimer(String name, Timer timer, int timeoutMs) {
        this.timer = timer;
        this.name = name;
        this.timeoutMs = timeoutMs;
        this.stop = true;
    }

    protected abstract void onTrigger();

    protected int adjustTimeout(final int timeoutMs) {
        return timeoutMs;
    }

    public void run() {
        this.invoking = true;
        try {
            onTrigger();
        } catch (Exception e) {
            //TODO LOG
        }

        this.lock.lock();
        try {
            this.invoking = false;
            if(this.stop) {
                this.running = false;
            } else {
                this.timeout = null;
                schedule();// again
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void schedule() {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
        TimerTask task = timeout -> {
            ReentrantTimer.this.run();
        };

        this.timeout = this.timer.newTimeout(task, adjustTimeout(this.timeoutMs), TimeUnit.MILLISECONDS);
    }

    public void start() {
        this.lock.lock();
        try {
            if (this.destroyed) {
                return;
            }
            if (!this.stop) {
                return;
            }
            stop = false;
            if (this.running) {
                return;
            }
            this.running = true;
            schedule();
        } finally {
            this.lock.unlock();
        }
    }

    public void destroy() {
        this.lock.lock();
        try {
            if (this.destroyed) {
                return;
            }
            this.destroyed = true;
            if (this.stop) {
                return;
            }
            this.stop = true;
            if (this.timeout != null) {
                if (this.timeout.cancel()) {
                    running = false;
                }
                this.timeout = null;
            }
        } finally {
            this.lock.unlock();
            this.timer.stop();
        }
    }

    public void stop() {
        this.lock.lock();
        try {
            if (this.stop) {
                return;
            }
            this.stop = true;
            if (this.timeout != null) {
                this.timeout.cancel();
                this.running = false;
                this.timeout = null;
            }
        } finally {
            this.lock.unlock();
        }
    }
}
