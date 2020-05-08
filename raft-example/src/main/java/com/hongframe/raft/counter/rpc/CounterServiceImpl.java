package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.Status;
import com.hongframe.raft.counter.CounterCallback;
import com.hongframe.raft.counter.CounterRaftServerStartup;
import com.hongframe.raft.entity.Task;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcRequests.*;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:24
 */
public class CounterServiceImpl implements CounterService {

    private static final Logger LOG = LoggerFactory.getLogger(CounterServiceImpl.class);

    private AtomicInteger value = new AtomicInteger();
    private CounterRaftServerStartup startup;

    public CounterServiceImpl(CounterRaftServerStartup startup) {

        this.startup = startup;

    }

    @Override
    public Response<ValueResponse> incrementAndGet(IncrementAndGetRequest request) {
        final AsyncContext asyncContext = RpcContext.startAsync();

        CounterCallback counterCallback = new CounterCallback(asyncContext);
        Task task = new Task();
        task.setCallback(new ResponseCallbackAdapter() {
            @Override
            public void run(Status status) {

            }
        });
        task.setData(ByteBuffer.allocate(32).putInt(value.get()));
        startup.getNode().apply(task);

        return null;
    }
}
