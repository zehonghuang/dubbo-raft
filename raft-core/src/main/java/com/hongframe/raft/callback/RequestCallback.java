package com.hongframe.raft.callback;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-03 20:45
 */
public interface RequestCallback extends Callback {

    void sendResponse(final Message msg);

}
