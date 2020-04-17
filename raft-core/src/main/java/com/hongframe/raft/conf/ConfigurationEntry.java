package com.hongframe.raft.conf;

import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.entity.PeerId;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 16:15
 */
public class ConfigurationEntry {

    private LogId id = new LogId();
    private Configuration conf = new Configuration();
    private Configuration oldConf = new Configuration();

    public ConfigurationEntry() {
    }

    public ConfigurationEntry(LogId id, Configuration conf, Configuration oldConf) {
        this.id = id;
        this.conf = conf;
        this.oldConf = oldConf;
    }

    public boolean contains(PeerId peerId) {
        return conf.contains(peerId);
    }

    public LogId getId() {
        return id;
    }

    public void setId(LogId id) {
        this.id = id;
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getOldConf() {
        return oldConf;
    }

    public void setOldConf(Configuration oldConf) {
        this.oldConf = oldConf;
    }
}
