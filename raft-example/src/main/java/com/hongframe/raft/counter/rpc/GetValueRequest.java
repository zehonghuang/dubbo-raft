package com.hongframe.raft.counter.rpc;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-16 15:10
 */
public class GetValueRequest implements Serializable {

    private boolean readOnlySafe = true;

    public boolean isReadOnlySafe() {
        return readOnlySafe;
    }

    public void setReadOnlySafe(boolean readOnlySafe) {
        this.readOnlySafe = readOnlySafe;
    }
}
