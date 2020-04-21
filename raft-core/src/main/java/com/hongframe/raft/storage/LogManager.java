package com.hongframe.raft.storage;

import com.hongframe.raft.entity.LogId;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 19:11
 */
public interface LogManager {

    long getLastLogIndex();

    long getLastLogIndex(final boolean isFlush);

    LogId getLastLogId(final boolean isFlush);

    long getTerm(final long index);

}
