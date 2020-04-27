package com.hongframe.raft.core;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.option.BallotBoxOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:20
 */
public class BallotBox implements Lifecycle<BallotBoxOptions> {
    @Override
    public boolean init(BallotBoxOptions opts) {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
