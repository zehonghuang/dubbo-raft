package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.core.RequestVoteRpc;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:25
 */
public class RpcRequests {

    public static final class AppendEntriesRequest implements Message {
        private String groupId;
        private String serverId;
        private String peerId;
        private Long term;
        private Long prevLogTerm;
        private Long preLogIndex;
        //TODO private EntryMeta entries;
        private Long committedIndex;
        //TODO privete byte[] data;


        @Override
        public String toString() {
            return "AppendEntriesRequest{" +
                    "groupId='" + groupId + '\'' +
                    ", serverId='" + serverId + '\'' +
                    ", peerId='" + peerId + '\'' +
                    ", term=" + term +
                    ", prevLogTerm=" + prevLogTerm +
                    ", preLogIndex=" + preLogIndex +
                    ", committedIndex=" + committedIndex +
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

        @Override
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

        public Long getPrevLogTerm() {
            return prevLogTerm;
        }

        public void setPrevLogTerm(Long prevLogTerm) {
            this.prevLogTerm = prevLogTerm;
        }

        public Long getPreLogIndex() {
            return preLogIndex;
        }

        public void setPreLogIndex(Long preLogIndex) {
            this.preLogIndex = preLogIndex;
        }

        public Long getCommittedIndex() {
            return committedIndex;
        }

        public void setCommittedIndex(Long committedIndex) {
            this.committedIndex = committedIndex;
        }
    }

    public static final class AppendEntriesResponse implements Message {
        private Long term;
        private Boolean success;
        private Long lastLogLast;

        @Override
        public String toString() {
            return "AppendEntriesResponse{" +
                    "term=" + term +
                    ", success=" + success +
                    ", lastLogLast=" + lastLogLast +
                    '}';
        }

        public Long getTerm() {
            return term;
        }

        public void setTerm(Long term) {
            this.term = term;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Long getLastLogLast() {
            return lastLogLast;
        }

        public void setLastLogLast(Long lastLogLast) {
            this.lastLogLast = lastLogLast;
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

    public static final class RequestVoteResponse implements Message {
        private Long term;
        private Boolean granted;
        private Boolean preVote;

        public Boolean getGranted() {
            return granted;
        }

        public void setGranted(Boolean granted) {
            this.granted = granted;
        }

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
        public String getPeerId() {
            return null;
        }

        @Override
        public String getGroupId() {
            return null;
        }

        @Override
        public String toString() {
            return "RequestVoteResponse{" +
                    "term=" + term +
                    ", preVote=" + preVote +
                    '}';
        }

        @Override
        public String seviceName() {
            return RequestVoteRpc.class.getSimpleName();
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

        @Override
        public String seviceName() {
            return RequestVoteRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            if(preVote)
                return "preVote";
            return "requestVote";
        }

        @Override
        public String getName() {
            return getClass().getName();
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

        public ErrorResponse() {
        }

        public ErrorResponse(Integer errorCode, String errorMsg) {
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }

        @Override
        public String getGroupId() {
            return null;
        }

        @Override
        public String getPeerId() {
            return null;
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

    public final static class Response<T extends Message> {
        private T data;
        private ErrorResponse error;

        public Response() {
        }

        public Response(ErrorResponse error) {
            this.error = error;
        }

        public Response(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public ErrorResponse getError() {
            return error;
        }

        public void setError(ErrorResponse error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "data=" + data +
                    ", error=" + error +
                    '}';
        }
    }

}
