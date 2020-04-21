package com.hongframe.raft.core;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

    ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit);

    void shutdown();

}
