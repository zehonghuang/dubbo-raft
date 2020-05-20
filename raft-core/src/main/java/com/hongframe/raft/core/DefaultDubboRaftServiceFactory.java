package com.hongframe.raft.core;

import com.hongframe.raft.DubboRaftServiceFactory;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.storage.impl.RocksDBLogStorage;
import com.hongframe.raft.storage.snapshot.SnapshotStorage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:53
 */
public class DefaultDubboRaftServiceFactory implements DubboRaftServiceFactory {
    @Override
    public LogStorage createLogStorage(String uri, RaftOptions raftOptions) {
        return new RocksDBLogStorage(uri);
    }

    @Override
    public SnapshotStorage createSnapshotStorage(String uri, RaftOptions raftOptions) {
        return null;
    }
}
