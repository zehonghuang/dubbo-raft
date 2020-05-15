package com.hongframe.raft.callback;

import com.hongframe.raft.Status;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 13:49
 */
public abstract class ReadIndexCallback implements Callback {

    public static final long INVALID_LOG_INDEX = -1;
    private long index = INVALID_LOG_INDEX;
    private byte[] requestContext;

    public abstract void run(final Status status, final long index, final byte[] reqCtx);

    public void setResult(final long index, final byte[] reqCtx) {
        this.index = index;
        this.requestContext = reqCtx;
    }

    public long getIndex() {
        return this.index;
    }

    public byte[] getRequestContext() {
        return this.requestContext;
    }


    @Override
    public void run(Status status) {
        run(status, this.index, this.requestContext);
    }
}
