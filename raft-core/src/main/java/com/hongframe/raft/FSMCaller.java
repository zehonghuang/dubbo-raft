package com.hongframe.raft;

import com.hongframe.raft.option.FSMCallerOptions;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 19:10
 */
public interface FSMCaller extends Lifecycle<FSMCallerOptions> {

    boolean onCommitted(final long committedIndex);


}
