package com.hongframe.raft.rpc.core;

import com.hongframe.raft.rpc.RpcRequests;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-01 13:28
 */
public interface GetFileRpc {

    RpcRequests.GetFileResponse getFile(RpcRequests.GetFileRequest request);

}
