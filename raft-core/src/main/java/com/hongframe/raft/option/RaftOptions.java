package com.hongframe.raft.option;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 01:41
 */
public class RaftOptions {

    private int maxByteCountPerRpc = 128 * 1024;

    private int maxEntriesSize = 1024;

    private int maxBodySize = 512 * 1024;

    private int maxElectionDelayMs = 1000;

    private int electionHeartbeatFactor = 10;

    private int disruptorBufferSize = 16384;

    private int applyBatch = 32;

    private int disruptorPublishEventWaitTimeoutSecs = 10;

    private int maxAppendBufferSize = 256 * 1024;

    private int maxReplicatorFlyingMsgs = 256;

    private boolean sync = true;

    private boolean syncMeta = false;

    private ReadOnlyOption readOnlyOptions = ReadOnlyOption.ReadOnlySafe;

    public ReadOnlyOption getReadOnlyOptions() {
        return readOnlyOptions;
    }

    public void setReadOnlyOptions(ReadOnlyOption readOnlyOptions) {
        this.readOnlyOptions = readOnlyOptions;
    }

    public int getMaxAppendBufferSize() {
        return maxAppendBufferSize;
    }

    public void setMaxAppendBufferSize(int maxAppendBufferSize) {
        this.maxAppendBufferSize = maxAppendBufferSize;
    }

    public int getDisruptorPublishEventWaitTimeoutSecs() {
        return disruptorPublishEventWaitTimeoutSecs;
    }

    public void setDisruptorPublishEventWaitTimeoutSecs(int disruptorPublishEventWaitTimeoutSecs) {
        this.disruptorPublishEventWaitTimeoutSecs = disruptorPublishEventWaitTimeoutSecs;
    }

    public int getApplyBatch() {
        return applyBatch;
    }

    public void setApplyBatch(int applyBatch) {
        this.applyBatch = applyBatch;
    }

    public int getDisruptorBufferSize() {
        return disruptorBufferSize;
    }

    public void setDisruptorBufferSize(int disruptorBufferSize) {
        this.disruptorBufferSize = disruptorBufferSize;
    }

    public int getMaxByteCountPerRpc() {
        return maxByteCountPerRpc;
    }

    public void setMaxByteCountPerRpc(int maxByteCountPerRpc) {
        this.maxByteCountPerRpc = maxByteCountPerRpc;
    }

    public int getMaxEntriesSize() {
        return maxEntriesSize;
    }

    public void setMaxEntriesSize(int maxEntriesSize) {
        this.maxEntriesSize = maxEntriesSize;
    }

    public int getMaxBodySize() {
        return maxBodySize;
    }

    public void setMaxBodySize(int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }

    public int getMaxElectionDelayMs() {
        return maxElectionDelayMs;
    }

    public void setMaxElectionDelayMs(int maxElectionDelayMs) {
        this.maxElectionDelayMs = maxElectionDelayMs;
    }

    public int getElectionHeartbeatFactor() {
        return electionHeartbeatFactor;
    }

    public void setElectionHeartbeatFactor(int electionHeartbeatFactor) {
        this.electionHeartbeatFactor = electionHeartbeatFactor;
    }

    public int getMaxReplicatorFlyingMsgs() {
        return maxReplicatorFlyingMsgs;
    }

    public void setMaxReplicatorFlyingMsgs(int maxReplicatorFlyingMsgs) {
        this.maxReplicatorFlyingMsgs = maxReplicatorFlyingMsgs;
    }

    public boolean isSync() {
        return this.sync;
    }

    public void setSync(final boolean sync) {
        this.sync = sync;
    }

    public boolean isSyncMeta() {
        return this.sync || this.syncMeta;
    }

    public void setSyncMeta(final boolean syncMeta) {
        this.syncMeta = syncMeta;
    }
}
