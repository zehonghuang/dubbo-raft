package com.hongframe.raft.option;

import com.hongframe.raft.conf.Configuration;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 21:23
 */
public class NodeOptions {

    private Configuration config;

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }
}
