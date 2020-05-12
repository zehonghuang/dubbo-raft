package com.hongframe.raft.core;

import com.hongframe.raft.Iterator;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;

import java.nio.ByteBuffer;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-08 15:59
 */
public class IteratorWrapper implements Iterator {

    private final IteratorImpl iterator;

    public IteratorWrapper(IteratorImpl iterator) {
        this.iterator = iterator;
    }

    @Override
    public long index() {
        return iterator.getIndex();
    }

    @Override
    public long term() {
        return iterator.entry().getId().getTerm();
    }

    @Override
    public ByteBuffer data() {
        final LogEntry entry = this.iterator.entry();
        return entry != null ? entry.getData() : null;
    }

    @Override
    public Callback callback() {
        return iterator.callback();
    }

    @Override
    public boolean hasNext() {
        return this.iterator.isGood() && this.iterator.entry().getType() == EntryType.ENTRY_TYPE_DATA;
    }

    @Override
    public ByteBuffer next() {
        final ByteBuffer data = data();
        if (hasNext()) {
            this.iterator.next();
        }
        return data;
    }
}
