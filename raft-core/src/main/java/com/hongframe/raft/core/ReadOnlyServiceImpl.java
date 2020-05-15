package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.ReadOnlyService;
import com.hongframe.raft.callback.ReadIndexCallback;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.ReadOnlyServiceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 18:15
 */
public class ReadOnlyServiceImpl implements ReadOnlyService {

    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyServiceImpl.class);

    private RaftOptions raftOptions;
    private NodeImpl node;
    private final Lock lock = new ReentrantLock();
    private FSMCaller fsmCaller;

    @Override
    public void addRequest(byte[] reqCtx, ReadIndexCallback callback) {

    }

    @Override
    public boolean init(ReadOnlyServiceOptions opts) {
        this.node = opts.getNode();
        this.fsmCaller = opts.getFsmCaller();
        this.raftOptions = opts.getRaftOptions();
        return false;
    }

    @Override
    public void shutdown() {

    }
}
