package com.hongframe.raft.slime.options;

import com.hongframe.raft.util.Utils;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 16:27
 */
public class RpcOptions {

    private int callbackExecutorCorePoolSize    = Utils.CPUS << 2;
    private int callbackExecutorMaximumPoolSize = Utils.CPUS << 3;
    private int callbackExecutorQueueCapacity   = 512;
    private int rpcTimeoutMillis                = 5000;

    public int getCallbackExecutorCorePoolSize() {
        return callbackExecutorCorePoolSize;
    }

    public void setCallbackExecutorCorePoolSize(int callbackExecutorCorePoolSize) {
        this.callbackExecutorCorePoolSize = callbackExecutorCorePoolSize;
    }

    public int getCallbackExecutorMaximumPoolSize() {
        return callbackExecutorMaximumPoolSize;
    }

    public void setCallbackExecutorMaximumPoolSize(int callbackExecutorMaximumPoolSize) {
        this.callbackExecutorMaximumPoolSize = callbackExecutorMaximumPoolSize;
    }

    public int getCallbackExecutorQueueCapacity() {
        return callbackExecutorQueueCapacity;
    }

    public void setCallbackExecutorQueueCapacity(int callbackExecutorQueueCapacity) {
        this.callbackExecutorQueueCapacity = callbackExecutorQueueCapacity;
    }

    public int getRpcTimeoutMillis() {
        return rpcTimeoutMillis;
    }

    public void setRpcTimeoutMillis(int rpcTimeoutMillis) {
        this.rpcTimeoutMillis = rpcTimeoutMillis;
    }

    @Override
    public String toString() {
        return "RpcOptions{" +
                "callbackExecutorCorePoolSize=" + callbackExecutorCorePoolSize +
                ", callbackExecutorMaximumPoolSize=" + callbackExecutorMaximumPoolSize +
                ", callbackExecutorQueueCapacity=" + callbackExecutorQueueCapacity +
                ", rpcTimeoutMillis=" + rpcTimeoutMillis +
                '}';
    }
}
