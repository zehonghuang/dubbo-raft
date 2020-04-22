package com.hongframe.raft.option;

import com.hongframe.raft.rpc.core.AppendEntriesRpc;
import com.hongframe.raft.rpc.core.ClientRequestRpc;
import com.hongframe.raft.rpc.core.MembershipChangeRpc;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.AppendEntriesRpcImpl;
import com.hongframe.raft.rpc.impl.ClientRequestRpcImpl;
import com.hongframe.raft.rpc.impl.MembershipChangeRpcImpl;
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

    private List<Class> clientServicesInterface = new ArrayList<>();
    private List<Class> clientServicesImpl = new ArrayList<>();

    private List<Class> userServicesInterface = new ArrayList<>();
    private List<Class> userServicesImpl = new ArrayList<>();

    public RpcRemoteOptions() {
        init();
    }

    private void init() {
        addRaftRequest();
        addClientRequest();
    }


    private void addRaftRequest() {
        addRaftRequest0(RequestVoteRpc.class, RequestVoteRpcImpl.class, null);
        addRaftRequest0(AppendEntriesRpc.class, AppendEntriesRpcImpl.class, null);
        addRaftRequest0(MembershipChangeRpc.class, MembershipChangeRpcImpl.class, null);
    }

    private void addRaftRequest0(Class interfacez, Class implz, Class mock) {
        servicesInterface.add(interfacez);
        servicesImpl.add(implz);
        servicesMock.add(mock);
    }

    private void addClientRequest() {
        addClientRequest0(ClientRequestRpc.class, ClientRequestRpcImpl.class);
    }

    private void addClientRequest0(Class interfacez, Class implz) {
        clientServicesInterface.add(interfacez);
        clientServicesImpl.add(implz);
    }

    public void registerUserService(Class serviceInterface, Class serviceImpl) {
        userServicesInterface.add(serviceInterface);
        userServicesImpl.add(serviceImpl);
    }

    public List<Class> getClientServicesInterface() {
        return clientServicesInterface;
    }

    public List<Class> getClientServicesImpl() {
        return clientServicesImpl;
    }

    public List<Class> getUserServicesInterface() {
        return userServicesInterface;
    }

    public List<Class> getUserServicesImpl() {
        return userServicesImpl;
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
