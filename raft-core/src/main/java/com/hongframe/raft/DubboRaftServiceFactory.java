package com.hongframe.raft;

import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.storage.snapshot.SnapshotStorage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-20 16:50
 */
public interface DubboRaftServiceFactory {

    LogStorage createLogStorage(final String uri, final RaftOptions raftOptions);

    SnapshotStorage createSnapshotStorage(final String uri, final RaftOptions raftOptions);

}
