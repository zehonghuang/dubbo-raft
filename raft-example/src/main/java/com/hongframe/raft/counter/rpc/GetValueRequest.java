package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-16 15:10
 */
public class GetValueRequest implements Message {

    private boolean readOnlySafe = true;

    public boolean isReadOnlySafe() {
        return readOnlySafe;
    }

    public void setReadOnlySafe(boolean readOnlySafe) {
        this.readOnlySafe = readOnlySafe;
    }

    @Override
    public String seviceName() {
        return CounterService.class.getSimpleName();
    }

    @Override
    public String method() {
        return "getValue";
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String toString() {
        return "GetValueRequest{" +
                "readOnlySafe=" + readOnlySafe +
                '}';
    }
}
