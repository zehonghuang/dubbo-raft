package com.hongframe.raft.rpc;

import com.hongframe.raft.Status;

public interface Callback {

    void run(Status status);

}
