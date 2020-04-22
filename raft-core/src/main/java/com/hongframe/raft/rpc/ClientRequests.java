package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.core.ClientRequestRpc;

public class ClientRequests {

    public final static class GetLeaderRequest implements Message {

        private String groupId;
        private String peerId;

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }

        @Override
        public String getPeerId() {
            return this.peerId;
        }

        @Override
        public String getGroupId() {
            return this.groupId;
        }

        @Override
        public String seviceName() {
            return ClientRequestRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "getLeader";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public final static class GetLeaderResponse implements Message {

        private String leaderId;

        public String getLeaderId() {
            return leaderId;
        }

        public void setLeaderId(String leaderId) {
            this.leaderId = leaderId;
        }

        @Override
        public String getPeerId() {
            return null;
        }

        @Override
        public String getGroupId() {
            return null;
        }
    }

}
