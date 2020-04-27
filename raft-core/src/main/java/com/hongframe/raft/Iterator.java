package com.hongframe.raft;

import com.hongframe.raft.callback.Callback;

import java.nio.ByteBuffer;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-26 22:46
 */
public interface Iterator extends java.util.Iterator<ByteBuffer> {

    long index();

    long term();

    ByteBuffer data();

    Callback callback();

}
