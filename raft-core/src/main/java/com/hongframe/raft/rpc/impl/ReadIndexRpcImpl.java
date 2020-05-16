package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.ReadIndexRpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-16 14:37
 */
public class ReadIndexRpcImpl implements ReadIndexRpc {
    @Override
    public Response<ReadIndexResponse> readIndex(ReadIndexRequest request) {
        Message message = getNode(request).handleReadIndexRequest(request, null);
        return checkResponse(message);
    }
}
