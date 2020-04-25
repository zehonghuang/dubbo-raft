package com.hongframe.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-25 18:17
 */
public class Platform {

    private static final Logger LOG = LoggerFactory.getLogger(Platform.class);

    private static final boolean IS_WINDOWS = isWindows0();

    private static final boolean IS_MAC = isMac0();

    /**
     * Return {@code true} if the JVM is running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * Return {@code true} if the JVM is running on Mac OSX
     */
    public static boolean isMac() {
        return IS_MAC;
    }

    private static boolean isMac0() {
        final boolean mac = SystemPropertyUtil.get("os.name", "") //
                .toLowerCase(Locale.US) //
                .contains("mac os x");
        if (mac) {
            LOG.debug("Platform: Mac OS X");
        }
        return mac;
    }

    private static boolean isWindows0() {
        final boolean windows = SystemPropertyUtil.get("os.name", "") //
                .toLowerCase(Locale.US) //
                .contains("win");
        if (windows) {
            LOG.debug("Platform: Windows");
        }
        return windows;
    }
}

