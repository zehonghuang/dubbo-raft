package com.hongframe.raft.option;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.codec.LogEntryCodecFactory;
import com.hongframe.raft.storage.LogStorage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-28 19:02
 */
public class LogManagerOptions {

    private LogStorage logStorage;
    private ConfigurationManager configurationManager;
    private FSMCaller caller;
    private int disruptorBufferSize = 1024;
    private RaftOptions raftOptions;
    private LogEntryCodecFactory logEntryCodecFactory;

    public LogStorage getLogStorage() {
        return logStorage;
    }

    public void setLogStorage(LogStorage logStorage) {
        this.logStorage = logStorage;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public FSMCaller getCaller() {
        return caller;
    }

    public void setCaller(FSMCaller caller) {
        this.caller = caller;
    }

    public int getDisruptorBufferSize() {
        return disruptorBufferSize;
    }

    public void setDisruptorBufferSize(int disruptorBufferSize) {
        this.disruptorBufferSize = disruptorBufferSize;
    }

    public RaftOptions getRaftOptions() {
        return raftOptions;
    }

    public void setRaftOptions(RaftOptions raftOptions) {
        this.raftOptions = raftOptions;
    }

    public LogEntryCodecFactory getLogEntryCodecFactory() {
        return logEntryCodecFactory;
    }

    public void setLogEntryCodecFactory(LogEntryCodecFactory logEntryCodecFactory) {
        this.logEntryCodecFactory = logEntryCodecFactory;
    }
}
