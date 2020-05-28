package com.hongframe.raft;

import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 01:13
 */
public interface StateMachine {

    void onApply(Iterator iterator);

    void onShutdown();

    void onLeaderStart(final long term);

    void onLeaderStop(Status status);

    void onSnapshotSave(final SnapshotWriter writer, final Callback callback);

    boolean onSnapshotLoad(final SnapshotReader reader);

}
