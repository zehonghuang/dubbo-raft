package com.hongframe.raft;

import com.hongframe.raft.callback.ReadIndexCallback;
import com.hongframe.raft.option.ReadOnlyServiceOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 14:53
 */
public interface ReadOnlyService extends Lifecycle<ReadOnlyServiceOptions> {

    void addRequest(final byte[] reqCtx, final ReadIndexCallback callback);

}
