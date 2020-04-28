package com.hongframe.raft.option;

import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.codec.LogEntryCodecFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-26 14:17
 */
public class LogStorageOptions {

    private ConfigurationManager configurationManager;
    private LogEntryCodecFactory codecFactory;

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public LogEntryCodecFactory getCodecFactory() {
        return codecFactory;
    }

    public void setCodecFactory(LogEntryCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }
}
