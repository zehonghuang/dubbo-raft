package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.rpc.RpcRequests.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:24
 */
public class CounterServiceImpl implements CounterService {

    private AtomicInteger value = new AtomicInteger();

    @Override
    public Response<ValueResponse> incrementAndGet(IncrementAndGetRequest request) {
        int v = value.addAndGet(request.getValue());
        return new Response<>(new ValueResponse(v, true));
    }
}
