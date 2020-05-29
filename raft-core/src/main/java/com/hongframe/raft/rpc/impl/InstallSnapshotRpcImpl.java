package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.RequestCallback;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.InstallSnapshotRpc;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-29 09:31
 */
public class InstallSnapshotRpcImpl implements InstallSnapshotRpc {
    @Override
    public Message intallSnapshot(InstallSnapshotRequest request) {

        final AsyncContext asyncContext = RpcContext.startAsync();

        RequestCallback callback = new RequestCallback() {
            private AsyncContext context = asyncContext;

            @Override
            public void sendResponse(Message msg) {
                this.context.signalContextSwitch();
                this.context.write(checkResponse(msg));
            }

            @Override
            public void run(Status status) {
                if (!status.isOk()) {
                    sendResponse(new ErrorResponse(status.getCode(), status.getErrorMsg()));
                }
            }
        };

        Message message = getNode(request).handleInstallSnapshotRequest(request, callback);

        if (message != null) {
            callback.sendResponse(message);
        }

        return null;
    }
}
