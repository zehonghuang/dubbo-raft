package com.hongframe.raft.core;

import com.hongframe.raft.util.NamedThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerManager implements Scheduler {

    private final ScheduledExecutorService service;

    public TimerManager(int workerNum) {
        this(workerNum, "Dubbo-raft-timerManager");
    }

    public TimerManager(int workerNum, String name) {
        this.service = Executors.newScheduledThreadPool(workerNum, new NamedThreadFactory(name, false));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return this.service.schedule(command, delay, unit);
    }

    @Override
    public void shutdown() {
        this.service.shutdownNow();
    }
}
