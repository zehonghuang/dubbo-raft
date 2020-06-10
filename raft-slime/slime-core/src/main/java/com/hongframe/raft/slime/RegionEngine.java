package com.hongframe.raft.slime;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.slime.options.RegionEngineOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 10:05
 */
public class RegionEngine implements Lifecycle<RegionEngineOptions> {
    @Override
    public boolean init(RegionEngineOptions opts) {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
