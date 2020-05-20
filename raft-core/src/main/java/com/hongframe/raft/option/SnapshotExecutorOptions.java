package com.hongframe.raft.option;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.util.Endpoint;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:32
 */
public class SnapshotExecutorOptions {

    private String uri;
    private FSMCaller fsmCaller;
    private NodeImpl node;
    private LogManager logManager;
    private long initTerm;
    private Endpoint addr;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public FSMCaller getFsmCaller() {
        return fsmCaller;
    }

    public void setFsmCaller(FSMCaller fsmCaller) {
        this.fsmCaller = fsmCaller;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public long getInitTerm() {
        return initTerm;
    }

    public void setInitTerm(long initTerm) {
        this.initTerm = initTerm;
    }

    public Endpoint getAddr() {
        return addr;
    }

    public void setAddr(Endpoint addr) {
        this.addr = addr;
    }
}
