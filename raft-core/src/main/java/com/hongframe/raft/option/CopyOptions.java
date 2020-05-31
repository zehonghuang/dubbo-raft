package com.hongframe.raft.option;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-31 23:46
 */
public class CopyOptions {

    private int maxRetry = 3;
    private long retryIntervalMs = 1000L;
    private int timeoutMs = 10 * 1000;

    public int getMaxRetry() {
        return this.maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public long getRetryIntervalMs() {
        return this.retryIntervalMs;
    }

    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public int getTimeoutMs() {
        return this.timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

}
