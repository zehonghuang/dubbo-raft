package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:22
 */
public class ValueResponse implements Message {

    private int value;
    private boolean success;

    public ValueResponse(int value, boolean success) {
        this.value = value;
        this.success = success;
    }

    @Override
    public String toString() {
        return "ValueResponse{" +
                "value=" + value +
                ", success=" + success +
                '}';
    }

    public ValueResponse() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
