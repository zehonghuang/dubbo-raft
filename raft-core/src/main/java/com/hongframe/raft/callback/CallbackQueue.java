package com.hongframe.raft.callback;

import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 18:25
 */
public interface CallbackQueue {

    void clear();

    void resetFirstIndex(final long firstIndex);

    void appendPendingClosure(final Callback callback);

    long popClosureUntil(final long endIndex, final List<Callback> callbacks);

}
