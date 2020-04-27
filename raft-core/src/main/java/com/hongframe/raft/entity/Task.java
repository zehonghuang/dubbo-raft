package com.hongframe.raft.entity;

import com.hongframe.raft.callback.Callback;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Task implements Serializable {

    private ByteBuffer data;
    private Callback callback;

    public Task() {
    }

    public Task(ByteBuffer data, Callback callback) {
        this.data = data;
        this.callback = callback;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
