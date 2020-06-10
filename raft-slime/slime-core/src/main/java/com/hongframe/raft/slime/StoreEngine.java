package com.hongframe.raft.slime;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.slime.options.StoreEngineOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 10:03
 */
public class StoreEngine implements Lifecycle<StoreEngineOptions> {
    @Override
    public boolean init(StoreEngineOptions opts) {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
