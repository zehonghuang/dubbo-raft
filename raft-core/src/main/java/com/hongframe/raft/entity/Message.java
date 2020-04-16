package com.hongframe.raft.entity;

public interface Message extends java.io.Serializable {

    default String seviceName() {
        return null;
    }

    default String method() {
        return null;
    }

    default String getName() {
        return null;
    }

}
