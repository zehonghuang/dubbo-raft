package com.hongframe.raft.counter;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.counter.rpc.ValueResponse;
import com.hongframe.raft.rpc.RpcRequests;
import org.apache.dubbo.rpc.AsyncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-09 00:08
 */
public class CounterCallback extends ResponseCallbackAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CounterCallback.class);

    private final AsyncContext asyncContext;

    private ValueResponse value;

    public CounterCallback(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public void run(Status status) {
        this.asyncContext.signalContextSwitch();
        LOG.info("value : {}", value);
        asyncContext.write(new RpcRequests.Response<>(this.value));
    }

    public void success(final long value) {
        final ValueResponse response = new ValueResponse();
        response.setValue(value);
        response.setSuccess(true);
        this.value = response;
    }
}
