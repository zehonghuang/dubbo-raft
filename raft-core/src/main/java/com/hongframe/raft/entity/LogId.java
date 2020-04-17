package com.hongframe.raft.entity;

import com.hongframe.raft.util.Copiable;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 16:17
 */
public class LogId implements Comparable<LogId>, Copiable<LogId>, Serializable {

    private long term;
    private long index;

    public LogId() {
        this(0, 0);
    }

    public LogId(long term, long index) {
        this.term = term;
        this.index = index;
    }

    @Override
    public LogId copy() {
        return new LogId(this.term, this.index);
    }

    @Override
    public int compareTo(LogId o) {
        final int c = Long.compare(getTerm(), o.getTerm());
        if (c == 0) {
            return Long.compare(getIndex(), o.getIndex());
        } else {
            return c;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.index ^ (this.index >>> 32));
        result = prime * result + (int) (this.term ^ (this.term >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogId other = (LogId) obj;
        if (this.index != other.index) {
            return false;
        }
        // noinspection RedundantIfStatement
        if (this.term != other.term) {
            return false;
        }
        return true;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}
