package com.hongframe.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 01:35
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NamedThreadFactory.class);

    private static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.error("uncaught Exception thread: {}", t, e);
        }
    };

    private final String name;
    private final boolean daemon;

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
        thread.setName(this.name);
        thread.setDaemon(this.daemon);
        return thread;
    }
}
