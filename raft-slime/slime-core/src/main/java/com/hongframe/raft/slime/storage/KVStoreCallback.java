package com.hongframe.raft.slime.storage;

import com.hongframe.raft.callback.Callback;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 10:07
 */
public interface KVStoreCallback extends Callback {

    int getError();

    void setError(final int error);//TODO Errors

    Object getData();

    void setData(final Object data);

}
