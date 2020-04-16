package com.hongframe.raft;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface Lifecycle<T> {

    boolean init(final T opts);

    void shutdown();

}
