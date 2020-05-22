package com.hongframe.raft.util;

import java.nio.ByteBuffer;

/**
 * fork form com.alipay.sofa.jraft.util.ByteBufferCollector
 * create time: 2020-05-22 11:21
 */
public class ByteBufferCollector {

    private static final int MAX_CAPACITY_TO_RECYCLE = 4 * 1024 * 1024; // 4M

    private ByteBuffer buffer;

    public int capacity() {
        return this.buffer != null ? this.buffer.capacity() : 0;
    }

    public void expandIfNecessary() {
        if (!hasRemaining()) {
            getBuffer(Utils.RAFT_DATA_BUF_SIZE);
        }
    }

    public void expandAtMost(final int atMostBytes) {
        if (this.buffer == null) {
            this.buffer = Utils.allocate(atMostBytes);
        } else {
            this.buffer = Utils.expandByteBufferAtMost(this.buffer, atMostBytes);
        }
    }

    public boolean hasRemaining() {
        return this.buffer != null && this.buffer.hasRemaining();
    }

    private ByteBufferCollector(final int size) {
        if (size > 0) {
            this.buffer = Utils.allocate(size);
        }
    }

    public static ByteBufferCollector allocate(final int size) {
        return new ByteBufferCollector(size);
    }

    public static ByteBufferCollector allocate() {
        return allocate(Utils.RAFT_DATA_BUF_SIZE);
    }

    private void reset(final int expectSize) {
        if (this.buffer == null) {
            this.buffer = Utils.allocate(expectSize);
        } else {
            if (this.buffer.capacity() < expectSize) {
                this.buffer = Utils.allocate(expectSize);
            }
        }
    }

    private ByteBuffer getBuffer(final int expectSize) {
        if (this.buffer == null) {
            this.buffer = Utils.allocate(expectSize);
        } else if (this.buffer.remaining() < expectSize) {
            this.buffer = Utils.expandByteBufferAtLeast(this.buffer, expectSize);
        }
        return this.buffer;
    }

    public void put(final ByteBuffer buf) {
        getBuffer(buf.remaining()).put(buf);
    }

    public void put(final byte[] bs) {
        getBuffer(bs.length).put(bs);
    }

    public void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

}
