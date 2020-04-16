package com.hongframe.raft;

public interface Lifecycle<T> {

    boolean init(final T opts);

    void shutdown();

}
