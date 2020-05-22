package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.SaveSnapshotCallback;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.SnapshotMeta;
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
    private volatile boolean savingSnapshot;
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

    private class SaveSnapshotDone implements SaveSnapshotCallback {
        SnapshotWriter writer;
        Callback callback;
        SnapshotMeta meta;

        public SaveSnapshotDone(SnapshotWriter writer, Callback callback, SnapshotMeta meta) {
            this.writer = writer;
            this.callback = callback;
            this.meta = meta;
        }

        @Override
        public void run(Status status) {
            //TODO onSnapshotSaveDone
        }

        @Override
        public SnapshotWriter start(SnapshotMeta meta) {
            this.meta = meta;
            return this.writer;
        }
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
            if (writer == null) {
                //TODO writer NPE
            }
            this.savingSnapshot = true;
            //TODO SaveSnapshotCallback & fsm.onSnapshotSave()
            SaveSnapshotDone done = new SaveSnapshotDone(writer, callback, null);
            if(!this.fsmCaller.onSnapshotSave(done)) {
                return;//TODO error
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
