package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.rpc.RpcRequests.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:14
 */
public interface CounterService {

    Response<ValueResponse> incrementAndGet(IncrementAndGetRequest request);

    Response<ValueResponse> getValue(GetValueRequest request);

}
