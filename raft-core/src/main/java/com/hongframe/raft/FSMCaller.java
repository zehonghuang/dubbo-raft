package com.hongframe.raft;

import com.hongframe.raft.callback.LoadSnapshotCallback;
import com.hongframe.raft.callback.SaveSnapshotCallback;
import com.hongframe.raft.option.FSMCallerOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 19:10
 */
public interface FSMCaller extends Lifecycle<FSMCallerOptions> {

    interface LastAppliedLogIndexListener {

        void onApplied(final long lastAppliedLogIndex);
    }

    void addLastAppliedLogIndexListener(final LastAppliedLogIndexListener listener);

    boolean onCommitted(final long committedIndex);

    boolean onLeaderStop(final Status status);

    boolean onLeaderStart(final long term);

    long getLastAppliedIndex();

    boolean onSnapshotSave(final SaveSnapshotCallback callback);

    boolean onSnapshotLoad(final LoadSnapshotCallback callback);


}
