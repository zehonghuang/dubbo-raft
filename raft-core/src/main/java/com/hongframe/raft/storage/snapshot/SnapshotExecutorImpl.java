package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.option.SnapshotExecutorOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.SnapshotExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:31
 */
public class SnapshotExecutorImpl implements SnapshotExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotExecutorImpl.class);

    private long term;
    private FSMCaller fsmCaller;
    private NodeImpl node;
    private LogManager logManager;

    @Override
    public NodeImpl getNode() {
        return null;
    }

    @Override
    public boolean init(SnapshotExecutorOptions opts) {
        this.logManager = opts.getLogManager();
        this.fsmCaller = opts.getFsmCaller();
        this.node = opts.getNode();
        this.term = opts.getInitTerm();
        return false;
    }

    @Override
    public void doSnapshot(Callback callback) {

    }

    @Override
    public void shutdown() {

    }
}
