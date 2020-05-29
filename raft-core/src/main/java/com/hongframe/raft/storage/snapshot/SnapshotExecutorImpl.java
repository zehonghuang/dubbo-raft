package com.hongframe.raft.storage.snapshot;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.LoadSnapshotCallback;
import com.hongframe.raft.callback.SaveSnapshotCallback;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.SnapshotMeta;
import com.hongframe.raft.option.SnapshotExecutorOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.storage.SnapshotExecutor;
import com.hongframe.raft.storage.snapshot.local.LocalSnapshotStorage;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
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
    private volatile boolean loadingSnapshot;
    private SnapshotStorage snapshotStorage;
    private FSMCaller fsmCaller;
    private NodeImpl node;
    private LogManager logManager;
    private SnapshotMeta loadingSnapshotMeta;

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

        this.snapshotStorage = new LocalSnapshotStorage(opts.getUri(), this.node.getNodeOptions().getRaftOptions());
        this.snapshotStorage.init(null);

        final SnapshotReader reader = this.snapshotStorage.open();
        if (reader == null) {
            return true;
        }
        this.loadingSnapshotMeta = reader.load();
        if (this.loadingSnapshotMeta == null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("", e);
            }
            return false;
        }
        LOG.info("Loading snapshot, meta={}.", this.loadingSnapshotMeta);
        this.loadingSnapshot = true;
        FirstSnapshotLoadDone loadDone = new FirstSnapshotLoadDone(reader);
        if (!this.fsmCaller.onSnapshotLoad(loadDone)) {
            return false;
        }
        return true;
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
            //TODO SaveSnapshotDone.run(status)
            onSnapshotSaveDone(status, this.writer, this.meta);
        }

        @Override
        public SnapshotWriter start(SnapshotMeta meta) {
            this.meta = meta;
            return this.writer;
        }
    }

    private class FirstSnapshotLoadDone implements LoadSnapshotCallback {

        private SnapshotReader reader;
        private CountDownLatch eventLatch;

        public FirstSnapshotLoadDone(SnapshotReader reader) {
            this.reader = reader;
            this.eventLatch = new CountDownLatch(1);
        }

        @Override
        public SnapshotReader start() {
            return this.reader;
        }

        @Override
        public void run(Status status) {
            onSnapshotLoadDone(status);
            this.eventLatch.countDown();
        }
    }

    @Override
    public void doSnapshot(Callback callback) {
        LOG.info("Node {} start do snapshot.", this.node.getNodeId());
        boolean doUnlock = true;
        this.lock.lock();
        try {
            if (this.savingSnapshot) {
                Utils.runInThread(() -> callback.run(new Status(10001, "Is saving another snapshot.")));
                return;
            }
            //TODO error
            if (this.fsmCaller.getLastAppliedIndex() == this.lastSnapshotIndex) {
                doUnlock = false;
                this.lock.unlock();
                LOG.info("Node {} last applied index equals last snaphot index: {}", this.node.getNodeId(), this.lastSnapshotIndex);
                this.logManager.clearBufferedLogs();
                Utils.runCallbackInThread(callback);
                return;
            }

            final long distance = this.fsmCaller.getLastAppliedIndex() - this.lastSnapshotIndex;
            LOG.info("Node {} distance: {}", this.node.getNodeId(), distance);
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
            if (!this.fsmCaller.onSnapshotSave(done)) {
                return;//TODO error
            }
        } finally {
            if (doUnlock) {
                this.lock.unlock();
            }
        }
    }

    @Override
    public SnapshotStorage getSnapshotStorage() {
        return this.snapshotStorage;
    }

    private int onSnapshotSaveDone(Status status, SnapshotWriter writer, SnapshotMeta meta) {
        int rest;
        this.lock.lock();
        try {
            rest = status.getCode();
            if (status.isOk()) {
                if (meta.getLastIncludedIndex() <= this.lastSnapshotIndex) {
                    //TODO writer setError
                }
            }
        } finally {
            this.lock.unlock();
        }

        if (rest == 0) {
            if (!writer.saveMeta(meta)) {
                //TODO writer.saveMeta fail
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            LOG.error("", e);
        }

        boolean doUnlock = true;
        this.lock.lock();
        try {
            if (rest == 0) {
                this.lastSnapshotIndex = meta.getLastIncludedIndex();
                this.lastSnapshotTerm = meta.getLastIncludedTerm();
                doUnlock = false;
                this.lock.unlock();
                this.logManager.setSnapshot(meta);
                doUnlock = true;
                this.lock.lock();
            } else {
                //TODO rest is error
            }
            this.savingSnapshot = false;
        } finally {
            if (doUnlock) {
                this.lock.unlock();
            }
        }

        return rest;
    }

    private void onSnapshotLoadDone(Status status) {
        boolean doUnlock = true;
        this.lock.lock();
        try {
            if (status.isOk()) {
                this.lastSnapshotIndex = this.loadingSnapshotMeta.getLastIncludedIndex();
                this.lastSnapshotTerm = this.loadingSnapshotMeta.getLastIncludedTerm();
                doUnlock = false;
                this.lock.unlock();
                this.logManager.setSnapshot(this.loadingSnapshotMeta);
                doUnlock = true;
                this.lock.lock();
            }

            this.loadingSnapshot = false;
//            this.downloadingSnapshot.set(null);
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
