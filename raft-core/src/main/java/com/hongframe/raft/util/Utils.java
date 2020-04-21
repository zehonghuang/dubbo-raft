package com.hongframe.raft.util;

import java.util.concurrent.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:42
 */
public class Utils {

    public static final int CPUS = SystemPropertyUtil.getInt(
            "jraft.available_processors", Runtime
                    .getRuntime().availableProcessors());

    public static final int MIN_EXECUTOR_POOL_SIZE = SystemPropertyUtil.getInt(
            "jraft.closure.threadpool.size.min",
            CPUS);

    public static final int MAX_EXECUTOR_POOL_SIZE = SystemPropertyUtil.getInt(
            "jraft.closure.threadpool.size.max",
            Math.max(100, CPUS * 5));


    private static final ThreadPoolExecutor GLOBAL_EXECUTOR = new ThreadPoolExecutor(MIN_EXECUTOR_POOL_SIZE,
            MAX_EXECUTOR_POOL_SIZE, 60l, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NamedThreadFactory("Global-Executor", false));


    public static final String IP_ANY = "0.0.0.0";

    public static long nowMs() {
        return System.currentTimeMillis();
    }
    public static Future<?> runInThread(final Runnable runnable) {
        return GLOBAL_EXECUTOR.submit(runnable);
    }


    public static long monotonicMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }


}
