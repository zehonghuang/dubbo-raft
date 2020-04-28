package com.hongframe.raft.option;

import com.hongframe.raft.StateMachine;
import com.hongframe.raft.conf.Configuration;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 21:23
 */
public class NodeOptions {

    private int electionTimeoutMs = 1000;

    private RaftOptions raftOptions = new RaftOptions();

    private Configuration config;

    private StateMachine stateMachine;

    private String logUri;

    private String raftMetaUri;

    public String getLogUri() {
        return logUri;
    }

    public void setLogUri(String logUri) {
        this.logUri = logUri;
    }

    public String getRaftMetaUri() {
        return raftMetaUri;
    }

    public void setRaftMetaUri(String raftMetaUri) {
        this.raftMetaUri = raftMetaUri;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public int getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public RaftOptions getRaftOptions() {
        return raftOptions;
    }

    public void setRaftOptions(RaftOptions raftOptions) {
        this.raftOptions = raftOptions;
    }
}
