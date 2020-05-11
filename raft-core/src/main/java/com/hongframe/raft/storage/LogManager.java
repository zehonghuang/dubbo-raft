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
            if (entries != null) {
                this.nEntries = entries.size();
            } else {
                this.nEntries = 0;
            }
        }

        public List<LogEntry> getEntries() {
            return entries;
        }

        public void setFirstLogIndex(long firstLogIndex) {
            this.firstLogIndex = firstLogIndex;
        }

        public FlushDoneCallback(final List<LogEntry> entries) {
            super();
            setEntries(entries);
        }
    }

    interface NewLogNotification {
        boolean onNewLog(final Object arg, final int errorCode);
    }

    long getFirstLogIndex();

    long getLastLogIndex();

    long getLastLogIndex(final boolean isFlush);

    LogId getLastLogId(final boolean isFlush);

    void setAppliedId(final LogId appliedId);

    long getTerm(final long index);

    LogEntry getEntry(final long index);

    void appendEntries(final List<LogEntry> entries, FlushDoneCallback callback);

    long wait(final long expectedLastLogIndex, final NewLogNotification notify, final Object arg);

}
