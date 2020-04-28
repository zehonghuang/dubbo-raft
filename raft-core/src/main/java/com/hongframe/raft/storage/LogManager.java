package com.hongframe.raft.storage;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.option.LogManagerOptions;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 19:11
 */
public interface LogManager extends Lifecycle<LogManagerOptions> {

    abstract class FlushDoneCallback implements Callback {
        private List<LogEntry> entries;
        protected int nEntries;
        protected long firstLogIndex = 0;

        public void setEntries(List<LogEntry> entries) {
            this.entries = entries;
        }

        public void setFirstLogIndex(long firstLogIndex) {
            this.firstLogIndex = firstLogIndex;
        }
    }

    long getLastLogIndex();

    long getLastLogIndex(final boolean isFlush);

    LogId getLastLogId(final boolean isFlush);

    long getTerm(final long index);

    void appendEntries(final List<LogEntry> entries, FlushDoneCallback callback);

    LogEntry getEntry(final long index);

}
