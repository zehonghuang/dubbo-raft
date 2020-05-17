package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.ReadIndexRpc;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-16 14:37
 */
public class ReadIndexRpcImpl implements ReadIndexRpc {
    @Override
    public Response<ReadIndexResponse> readIndex(ReadIndexRequest request) {
        final AsyncContext asyncContext = RpcContext.startAsync();

        ResponseCallbackAdapter adapter = new ResponseCallbackAdapter() {
            @Override
            public void run(Status status) {
                asyncContext.signalContextSwitch();
                if(status.isOk()) {
                    asyncContext.write(checkResponse(getResponse()));
                } else {
                    asyncContext.write(checkResponse(new ErrorResponse(status.getCode(), status.getErrorMsg())));
                }
            }
        };

        Message message = getNode(request).handleReadIndexRequest(request, adapter);
        if(message != null) {
            if(message instanceof ErrorResponse) {
                adapter.run(new Status(10001, ((ErrorResponse) message).getErrorMsg()));
            } else {
                adapter.setResponse(message);
                adapter.run(Status.OK());
            }
        }
        return null;
    }
}
