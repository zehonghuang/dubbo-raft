package com.hongframe.raft.rpc;

import com.hongframe.raft.Status;
import com.hongframe.raft.entity.Message;

import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-18 15:51
 */
public abstract class ResponseCallbackAdapter implements ResponseCallback {

    private Message message ;

    private void setResponse(Message message) {
        this.message = message;
    }

    @Override
    public Message getResponse() {
        return this.message;
    }

    public void invoke(RpcRequests.Response response) {
        Message message = response.getData();
        if(Objects.nonNull(message)) {
            setResponse(message);
            run(Status.OK());
        }
    }
}
