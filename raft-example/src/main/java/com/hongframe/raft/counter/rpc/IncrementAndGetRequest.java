package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:16
 */
public class IncrementAndGetRequest implements Message {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String seviceName() {
        return CounterService.class.getSimpleName();
    }

    @Override
    public String method() {
        return "incrementAndGet";
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
