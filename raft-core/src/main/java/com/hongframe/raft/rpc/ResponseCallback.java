package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-18 15:39
 */
public interface ResponseCallback extends Callback {

    Message getResponse();

}
