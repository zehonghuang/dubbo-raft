package com.hongframe.raft.util;

import java.util.concurrent.TimeUnit;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:42
 */
public class Utils {

    public static final int CPUS = SystemPropertyUtil.getInt(
            "jraft.available_processors", Runtime
                    .getRuntime().availableProcessors());


    public static final String IP_ANY = "0.0.0.0";

    public static long monotonicMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }


}
