package com.hongframe.raft.storage.snapshot.remote;

import com.hongframe.raft.core.Scheduler;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.CopyOptions;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.SnapshotCopierOptions;
import com.hongframe.raft.rpc.RpcClient;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.ByteBufferCollector;

import java.io.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-31 10:31
 */
public class RemoteFileCopier {

    private long readId;
    private RpcClient rpcClient;
    private PeerId peerId;//TODO endpoint
    private RaftOptions raftOptions;
    private Scheduler timerManager;

    public boolean init(String uri, final SnapshotCopierOptions opts) {
        this.rpcClient = opts.getRpcClient();
        this.timerManager = opts.getTimerManager();
        this.raftOptions = opts.getRaftOptions();
        //TODO init peerId and readId
        return true;
    }

    public Session startCopy2IoBuffer(final String source, final ByteBufferCollector destBuf, final CopyOptions opts) {
        final CopySession session = newCopySession(source);
        session.setOutputStream(null);
        session.setDestBuf(destBuf);
        if (opts != null) {
            session.setCopyOptions(opts);
        }
        session.sendNextRpc();
        return session;
    }

    public Session startCopyToFile(final String source, final String destPath, final CopyOptions opts) throws IOException {
        final File file = new File(destPath);
        if (file.exists()) {
            if (!file.delete()) {
                return null;
            }
        }
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false) {

            @Override
            public void close() throws IOException {
                getFD().sync();
                super.close();
            }
        });
        final CopySession session = newCopySession(source);
        session.setOutputStream(out);
        session.setDestPath(destPath);
        session.setDestBuf(null);
        if (opts != null) {
            session.setCopyOptions(opts);
        }
        session.sendNextRpc();
        return session;//TODO startCopyToFile
    }

    private CopySession newCopySession(final String source) {
        final RpcRequests.GetFileRequest request = new RpcRequests.GetFileRequest();
        request.setFilename(source);
        request.setReaderId(this.readId);
        return new CopySession(this.rpcClient, this.timerManager, this.raftOptions, request, this.peerId);
    }

}
