package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.rpc.RpcRequests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:24
 */
public class CounterServiceImpl implements CounterService {

    private static final Logger LOG = LoggerFactory.getLogger(CounterServiceImpl.class);

    private AtomicInteger value = new AtomicInteger();

    @Override
    public Response<ValueResponse> incrementAndGet(IncrementAndGetRequest request) {
        int v = value.addAndGet(request.getValue());
        LOG.info("value : {}", v);
        return new Response<>(new ValueResponse(v, true));
    }
}
