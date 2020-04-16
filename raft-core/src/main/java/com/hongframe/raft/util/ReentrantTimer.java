package com.hongframe.raft.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.Timer;

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


    public ReentrantTimer(String name, int timeoutMs) {
        this(name, new HashedWheelTimer(), timeoutMs);
    }

    public ReentrantTimer(String name, Timer timer, int timeoutMs) {
        this.timer = timer;
        this.name = name;
        this.timeoutMs = timeoutMs;
        this.stop = true;
    }

    public void start() {
        this.lock.lock();
        try {
            if(this.destroyed) {
                return;
            }
            if(!this.stop) {
                return;
            }
            stop = true;
            if(this.running) {
                return;
            }
            this.running = true;
            // TODO run task
        } finally {
            this.lock.unlock();
        }
    }

    public void stop() {
        this.lock.lock();
        try {
            if(this.stop) {
                return;
            }
            this.stop = true;
            if(this.timeout != null) {
                this.timeout.cancel();
                this.running = false;
                this.timeout = null;
            }
        } finally {
            this.lock.unlock();
        }
    }
}
