package com.hongframe.raft.counter.rpc;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ReadIndexCallback;
import com.hongframe.raft.counter.CounterCallback;
import com.hongframe.raft.counter.CounterRaftServerStartup;
import com.hongframe.raft.entity.Task;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.util.Bits;
import com.hongframe.raft.util.Bytes;
import com.hongframe.raft.util.Utils;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:24
 */
public class CounterServiceImpl implements CounterService {

    private static final Logger LOG = LoggerFactory.getLogger(CounterServiceImpl.class);

    private CounterRaftServerStartup startup;

    public CounterServiceImpl(CounterRaftServerStartup startup) {

        this.startup = startup;

    }

    @Override
    public Response<ValueResponse> incrementAndGet(IncrementAndGetRequest request) {
        final AsyncContext asyncContext = RpcContext.startAsync();

        CounterCallback counterCallback = new CounterCallback(asyncContext);
        Task task = new Task();
        task.setCallback(counterCallback);
        final byte[] v = new byte[8];
        Bits.putLong(v, 0, request.getValue());
        task.setData(ByteBuffer.wrap(v));
        startup.getNode().apply(task);

        return null;
    }

    @Override
    public Response<ValueResponse> getValue(GetValueRequest request) {
        final AsyncContext asyncContext = RpcContext.startAsync();

        final CounterCallback counterCallback = new CounterCallback(asyncContext);

        startup.getNode().readIndex(Bytes.EMPTY_BYTES, new ReadIndexCallback() {
            @Override
            public void run(Status status, long index, byte[] reqCtx) {
                if (status.isOk()) {
                    counterCallback.success(CounterServiceImpl.this.startup.getFsm().getValue());
                    counterCallback.run(Status.OK());
                } else {
                    //TODO ERROR
                }
            }
        });
        return null;
    }
}
