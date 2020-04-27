package com.hongframe.raft.callback;

import com.hongframe.raft.Status;

public interface Callback {

    void run(Status status);

}
