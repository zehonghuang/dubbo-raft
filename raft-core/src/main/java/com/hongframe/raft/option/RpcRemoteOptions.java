package com.hongframe.raft.option;

import com.hongframe.raft.rpc.core.AppendEntriesRpc;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.AppendEntriesRpcImpl;
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
    private List<Class> servicesMock = new ArrayList<>();

    public RpcRemoteOptions() {
        init();
    }

    private void init() {
        addRaftRequest();
    }


    private void addRaftRequest() {
        addRaftRequest0(RequestVoteRpc.class, RequestVoteRpcImpl.class, null);
        addRaftRequest0(AppendEntriesRpc.class, AppendEntriesRpcImpl.class, null);
    }

    private void addRaftRequest0(Class interfacez, Class implz, Class mock) {
        servicesInterface.add(interfacez);
        servicesImpl.add(implz);
        servicesMock.add(mock);
    }


    public List<Class> getServicesInterface() {
        return servicesInterface;
    }

    public List<Class> getServicesImpl() {
        return servicesImpl;
    }

    public List<Class> getServicesMock() {
        return servicesMock;
    }
}
