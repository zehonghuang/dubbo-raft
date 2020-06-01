package com.hongframe.raft.storage.snapshot.remote;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-01 13:17
 */
public class CopySession implements Session {

    private static final Logger LOG = LoggerFactory.getLogger(CopySession.class);

    private RaftOptions raftOptions;
    private Scheduler timerManager;
    private RpcClient rpcClient;
    private RpcRequests.GetFileRequest request;
    private final GetFileResponseCallback callback = new GetFileResponseCallback();
    private PeerId peerId;

    private class GetFileResponseCallback extends ResponseCallbackAdapter {
        @Override
        public void run(Status status) {

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

    @Override
    public void cancel() {

    }

    @Override
    public void join() throws InterruptedException {

    }

    @Override
    public Status status() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
