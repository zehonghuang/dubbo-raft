package com.hongframe.raft.storage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-03 22:13
 */
public interface SnapshotThrottle {

    long throttledByThroughput(final long bytes);

}
