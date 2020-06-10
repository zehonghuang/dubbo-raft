package com.hongframe.raft.slime.storage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 15:42
 */
public abstract class BaseKVStoreCallback implements KVStoreCallback {

    private volatile int error;
    private volatile Object data;

    @Override
    public int getError() {
        return error;
    }

    @Override
    public void setError(int error) {
        this.error = error;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }
}
