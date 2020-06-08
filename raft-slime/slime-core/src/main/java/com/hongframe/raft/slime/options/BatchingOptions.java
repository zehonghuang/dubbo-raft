package com.hongframe.raft.slime.options;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-08 22:01
 */
public class BatchingOptions {

    private boolean allowBatching = true;

    private int batchSize = 100;

    public boolean isAllowBatching() {
        return allowBatching;
    }

    public void setAllowBatching(boolean allowBatching) {
        this.allowBatching = allowBatching;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public String toString() {
        return "BatchingOptions{" +
                "allowBatching=" + allowBatching +
                ", batchSize=" + batchSize +
                '}';
    }
}
