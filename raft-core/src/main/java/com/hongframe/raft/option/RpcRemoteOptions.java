package com.hongframe.raft.option;

import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.RequestVoteRpcImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 23:39
 */
public class RpcRemoteOptions {

    private List<Class> servicesInterface = new ArrayList<>();
    private List<Class> servicesImpl = new ArrayList<>();

    public RpcRemoteOptions() {
        init();
    }

    private void init() {
        addRaftRequest();
    }

    private void addRaftRequest() {
        servicesInterface.add(RequestVoteRpc.class);
        servicesImpl.add(RequestVoteRpcImpl.class);
    }

    public List<Class> getServicesInterface() {
        return servicesInterface;
    }

    public List<Class> getServicesImpl() {
        return servicesImpl;
    }
}
