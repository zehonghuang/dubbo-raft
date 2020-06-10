package com.hongframe.raft.slime.rpc;

import com.hongframe.raft.slime.client.cmd.store.BaseRequest;
import com.hongframe.raft.slime.client.cmd.store.BaseResponse;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 17:45
 */
public interface KVCommandService {

    BaseResponse handleRequest(BaseRequest request);

}
