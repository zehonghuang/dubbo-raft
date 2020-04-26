package com.hongframe.raft.storage;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.option.LogStorageOptions;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-25 01:30
 */
public interface LogStorage extends Lifecycle<LogStorageOptions> {

    long getFirstLogIndex();

    long getLastLogIndex();

    LogEntry getEntry(final long index);

    long getTerm(final long index);

    boolean appendEntry(final LogEntry entry);

    int appendEntries(final List<LogEntry> entries);

}
