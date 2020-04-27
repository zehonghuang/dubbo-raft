package com.hongframe.raft.callback;

import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.rpc.RpcRequests;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 20:35
 */
public interface Invokeable extends Callback {

    default void invoke(RpcRequests.Response response) {

    }

}
