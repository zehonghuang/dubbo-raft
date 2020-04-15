package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 20:35
 */
public interface Callback {

    void run(Message message);

}
