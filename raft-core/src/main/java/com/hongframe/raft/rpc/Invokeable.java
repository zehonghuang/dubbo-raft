package com.hongframe.raft.rpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 20:35
 */
public interface Invokeable extends Callback {

    default void invoke(RpcRequests.Response response) {

    }

}
