package com.hongframe.raft.storage.snapshot.remote;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.CopyOptions;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.ByteBufferCollector;
import com.hongframe.raft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-01 13:17
 */
public class CopySession implements Session {

    private static final Logger LOG = LoggerFactory.getLogger(CopySession.class);

    private final Lock lock = new ReentrantLock();
    private final Status status = Status.OK();
    private RaftOptions raftOptions;
    private Scheduler timerManager;
    private RpcClient rpcClient;
    private RpcRequests.GetFileRequest request;
    private final GetFileResponseCallback callback = new GetFileResponseCallback();
    private PeerId peerId;
    private final CountDownLatch await = new CountDownLatch(1);
    private boolean finished;
    private ByteBufferCollector destBuf;
    private CopyOptions copyOptions = new CopyOptions();
    private OutputStream outputStream;
    private ScheduledFuture<?> timer;
    private CompletableFuture<?> rpcCall;

    private class GetFileResponseCallback extends ResponseCallbackAdapter {
        @Override
        public void run(Status status) {
            onRpcReturned(status, (RpcRequests.GetFileResponse) getResponse());
        }
    }

    public CopySession(final RpcClient rpcClient, final Scheduler timerManager, final RaftOptions raftOptions,
                       final RpcRequests.GetFileRequest request, final PeerId peerId) {
        this.raftOptions = raftOptions;
        this.timerManager = timerManager;
        this.rpcClient = rpcClient;
        this.request = request;
        this.peerId = peerId;
    }

    public void setDestBuf(final ByteBufferCollector bufRef) {
        this.destBuf = bufRef;
    }

    public void setCopyOptions(final CopyOptions copyOptions) {
        this.copyOptions = copyOptions;
    }

    public void setOutputStream(final OutputStream out) {
        this.outputStream = out;
    }

    @Override
    public void cancel() {
        this.lock.lock();
        try {
            if (this.finished) {
                return;
            }
            if (this.timer != null) {
                this.timer.cancel(true);
            }
            if (this.rpcCall != null) {
                this.rpcCall.cancel(true);
            }
            if (this.status.isOk()) {
                //TODO status setError
            }
            onFinished();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void join() throws InterruptedException {
        this.await.await();
    }

    @Override
    public Status status() {
        return this.status;
    }

    void sendNextRpc() {
        this.lock.lock();
        try {
            this.timer = null;
            long offset = this.request.getOffset() + this.request.getCount();
            final long maxCount = this.destBuf == null ? this.raftOptions.getMaxByteCountPerRpc() : Integer.MAX_VALUE;
            this.request.setOffset(offset);
            this.request.setCount(maxCount);
            this.request.setReadPartly(true);
            if (this.finished) {
                return;
            }
            this.rpcCall = this.rpcClient.getFile(this.peerId, this.request, this.callback);
        } finally {
            this.lock.unlock();
        }
    }

    private void onRpcReturned(Status status, RpcRequests.GetFileResponse response) {
        this.lock.lock();
        try {
            if (this.finished) {
                return;
            }
            if (!status.isOk()) {
                //TODO onRpcReturned not ok
                this.timer = this.timerManager.schedule(() -> Utils.runInThread(this::sendNextRpc),
                        this.copyOptions.getRetryIntervalMs(), TimeUnit.MILLISECONDS);
                return;
            }
            if (this.outputStream != null) {

            } else {
                this.destBuf.put(ByteBuffer.wrap(response.getData()));
            }
            if (!response.getEof()) {
                this.request.setCount(response.getReadSize());
            }
            if (response.getEof()) {
                onFinished();
                return;
            }
        } finally {
            this.lock.unlock();
        }
        sendNextRpc();
    }

    private void onFinished() {
        if (!this.finished) {
            if (!this.status.isOk()) {
                LOG.error("Fail to copy data, readerId={} fileName={} offset={} status={}",
                        this.request.getReaderId(), this.request.getFilename(),
                        this.request.getOffset(), this.status);
            }
            if (this.outputStream != null) {
                try {
                    this.outputStream.close();
                } catch (IOException e) {
                    LOG.error("", e);
                }
                this.outputStream = null;
            }
            if (this.destBuf != null) {
                final ByteBuffer buf = this.destBuf.getBuffer();
                if (buf != null) {
                    buf.flip();
                }
                this.destBuf = null;
            }
            this.finished = true;
            this.await.countDown();
        }
    }

    @Override
    public void close() throws IOException {
        this.lock.lock();
        try {
            if (!this.finished) {
                if (this.outputStream != null) {
                    try {
                        this.outputStream.close();
                    } catch (IOException e) {
                        LOG.error("", e);
                        throw e;
                    }
                    this.outputStream = null;
                }
            }
        } finally {
            this.lock.unlock();
        }
    }
}
