package com.hongframe.raft.slime.client;

import com.hongframe.raft.slime.options.SlimeStoreOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-09 00:58
 */
public class DefaultSlimeStore implements SlimeStore {
    @Override
    public boolean init(SlimeStoreOptions opts) {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
