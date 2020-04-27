package com.hongframe.raft.option;

import com.hongframe.raft.StateMachine;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.CallbackQueue;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.storage.LogManager;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 19:57
 */
public class FSMCallerOptions {

    private LogManager logManager;
    private StateMachine fsm;
    private Callback afterShutdown;
    private LogId bootstrapId;
    private CallbackQueue callbackQueue;
    private NodeImpl node;

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public StateMachine getFsm() {
        return fsm;
    }

    public void setFsm(StateMachine fsm) {
        this.fsm = fsm;
    }

    public Callback getAfterShutdown() {
        return afterShutdown;
    }

    public void setAfterShutdown(Callback afterShutdown) {
        this.afterShutdown = afterShutdown;
    }

    public LogId getBootstrapId() {
        return bootstrapId;
    }

    public void setBootstrapId(LogId bootstrapId) {
        this.bootstrapId = bootstrapId;
    }

    public CallbackQueue getCallbackQueue() {
        return callbackQueue;
    }

    public void setCallbackQueue(CallbackQueue callbackQueue) {
        this.callbackQueue = callbackQueue;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }
}
