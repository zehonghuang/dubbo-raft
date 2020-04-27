package com.hongframe.raft.option;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.callback.CallbackQueue;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 00:21
 */
public class BallotBoxOptions {

    private FSMCaller caller;
    private CallbackQueue callbackQueue;

    public FSMCaller getCaller() {
        return caller;
    }

    public void setCaller(FSMCaller caller) {
        this.caller = caller;
    }

    public CallbackQueue getCallbackQueue() {
        return callbackQueue;
    }

    public void setCallbackQueue(CallbackQueue callbackQueue) {
        this.callbackQueue = callbackQueue;
    }
}
