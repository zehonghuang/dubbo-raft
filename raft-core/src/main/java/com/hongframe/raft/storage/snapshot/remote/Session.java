package com.hongframe.raft.storage.snapshot.remote;

import com.hongframe.raft.Status;

import java.io.Closeable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-30 20:45
 */
public interface Session extends Closeable {

    void cancel();

    void join() throws InterruptedException;

    Status status();

}
