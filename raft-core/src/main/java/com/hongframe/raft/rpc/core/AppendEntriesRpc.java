package com.hongframe.raft.rpc.core;

import static com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface AppendEntriesRpc extends RaftRpcService {

    Response<AppendEntriesResponse> appendEntries(AppendEntriesRequest request);

}
