package com.hongframe.raft.rpc.core;

import com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-16 14:30
 */
public interface ReadIndexRpc extends RaftRpcService {

    Response<ReadIndexResponse> readIndex(ReadIndexRequest request);

}
