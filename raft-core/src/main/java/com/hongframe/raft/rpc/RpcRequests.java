package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:25
 */
public class RpcRequests {

    public static final class RequestVoteResponse implements Message {
        private Long term;
        private Boolean preVote;

        public Long getTerm() {
            return term;
        }

        public void setTerm(Long term) {
            this.term = term;
        }

        public Boolean getPreVote() {
            return preVote;
        }

        public void setPreVote(Boolean preVote) {
            this.preVote = preVote;
        }

        @Override
        public String toString() {
            return "RequestVoteResponse{" +
                    "term=" + term +
                    ", preVote=" + preVote +
                    '}';
        }
    }

    public static final class RequestVoteRequest implements Message {
        private String groupId;
        private String serverId;
        private String peerId;
        private Long term;
        private Long lastLogTerm;
        private Long LastLogIndex;
        private boolean preVote;

        @Override
        public String toString() {
            return "RequestVoteRequest{" +
                    "groupId='" + groupId + '\'' +
                    ", serverId='" + serverId + '\'' +
                    ", peerId='" + peerId + '\'' +
                    ", term=" + term +
                    ", lastLogTerm=" + lastLogTerm +
                    ", LastLogIndex=" + LastLogIndex +
                    ", preVote=" + preVote +
                    '}';
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
        }

        public String getPeerId() {
            return peerId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }

        public Long getTerm() {
            return term;
        }

        public void setTerm(Long term) {
            this.term = term;
        }

        public Long getLastLogTerm() {
            return lastLogTerm;
        }

        public void setLastLogTerm(Long lastLogTerm) {
            this.lastLogTerm = lastLogTerm;
        }

        public Long getLastLogIndex() {
            return LastLogIndex;
        }

        public void setLastLogIndex(Long lastLogIndex) {
            LastLogIndex = lastLogIndex;
        }

        public boolean isPreVote() {
            return preVote;
        }

        public void setPreVote(boolean preVote) {
            this.preVote = preVote;
        }
    }

    public final static class ErrorResponse implements Message {
        private Integer errorCode;
        private String errorMsg;

        @Override
        public String toString() {
            return "ErrorResponse{" +
                    "errorCode=" + errorCode +
                    ", errorMsg='" + errorMsg + '\'' +
                    '}';
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(Integer errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

}
