package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.option.SnapshotExecutorOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.SnapshotExecutor;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:31
 */
public class SnapshotExecutorImpl implements SnapshotExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotExecutorImpl.class);

    private final Lock lock = new ReentrantLock();
    private long lastSnapshotTerm;
    private long lastSnapshotIndex;
    private long term;
    private SnapshotStorage snapshotStorage;
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
        boolean doUnlock = true;
        this.lock.lock();
        try {
            //TODO error
            if (this.fsmCaller.getLastAppliedIndex() == this.lastSnapshotIndex) {
                doUnlock = false;
                this.lock.unlock();
                this.logManager.clearBufferedLogs();
                Utils.runCallbackInThread(callback);
                return;
            }

            final long distance = this.fsmCaller.getLastAppliedIndex() - this.lastSnapshotIndex;
            if (distance < this.node.getNodeOptions().getSnapshotLogIndexMargin()) {
                //TODO SnapshotLogIndexMargin = 0
            }
            //TODO SnapshotWriter
            SnapshotWriter writer = this.snapshotStorage.create();
            if(writer == null) {
                //TODO writer NPE
            }

        } finally {
            if (doUnlock) {
                this.lock.unlock();
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
