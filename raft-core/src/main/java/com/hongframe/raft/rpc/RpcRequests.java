package com.hongframe.raft.rpc;

import com.hongframe.raft.entity.*;
import com.hongframe.raft.rpc.core.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:25
 */
public class RpcRequests {

    public static final class ChangePeersRequest implements Message {
        private String groupId;
        private String leaderId;
        private String newPeers;

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getLeaderId() {
            return leaderId;
        }

        public void setLeaderId(String leaderId) {
            this.leaderId = leaderId;
        }

        public String getNewPeers() {
            return newPeers;
        }

        public void setNewPeers(String newPeers) {
            this.newPeers = newPeers;
        }

        @Override
        public String getPeerId() {
            return null;
        }

        @Override
        public String getGroupId() {
            return this.groupId;
        }

        @Override
        public String seviceName() {
            return MembershipChangeRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "changePeer";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public static final class ChangePeersResponse implements Message {

        private String oldPeers;
        private String newPeers;

        public String getOldPeers() {
            return oldPeers;
        }

        public void setOldPeers(String oldPeers) {
            this.oldPeers = oldPeers;
        }

        public String getNewPeers() {
            return newPeers;
        }

        public void setNewPeers(String newPeers) {
            this.newPeers = newPeers;
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

    public static final class RemovePeerRequest implements Message {
        private String leaderId;
        private String peerId;
        private String groupId;

        public String getLeaderId() {
            return leaderId;
        }

        public void setLeaderId(String leaderId) {
            this.leaderId = leaderId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
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
            return MembershipChangeRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "removePeer";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public static final class RemovePeerResponse implements Message {
        private String oldPeers;
        private String newPeers;

        @Override
        public String getPeerId() {
            return null;
        }

        @Override
        public String getGroupId() {
            return null;
        }

        public String getOldPeers() {
            return oldPeers;
        }

        public void setOldPeers(String oldPeers) {
            this.oldPeers = oldPeers;
        }

        public String getNewPeers() {
            return newPeers;
        }

        public void setNewPeers(String newPeers) {
            this.newPeers = newPeers;
        }
    }

    public static final class AddPeerRequest implements Message {

        private String leaderId;
        private String peerId;
        private String groupId;

        public String getLeaderId() {
            return leaderId;
        }

        public void setLeaderId(String leaderId) {
            this.leaderId = leaderId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
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
            return MembershipChangeRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "addPeer";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public static final class AddPeerResponse implements Message {
        private String oldPeers;
        private String newPeers;

        @Override
        public String getPeerId() {
            return null;
        }

        @Override
        public String getGroupId() {
            return null;
        }

        public String getOldPeers() {
            return oldPeers;
        }

        public void setOldPeers(String oldPeers) {
            this.oldPeers = oldPeers;
        }

        public String getNewPeers() {
            return newPeers;
        }

        public void setNewPeers(String newPeers) {
            this.newPeers = newPeers;
        }
    }

    public static final class OutLogEntry implements Serializable {
        private EntryType type;
        private LogId id = new LogId(0, 0);
        private List<PeerId> peers;
        private List<PeerId> oldPeers;
        private byte[] data;

        public EntryType getType() {
            return type;
        }

        public void setType(EntryType type) {
            this.type = type;
        }

        public LogId getId() {
            return id;
        }

        public void setId(LogId id) {
            this.id = id;
        }

        public List<PeerId> getPeers() {
            return peers;
        }

        public void setPeers(List<PeerId> peers) {
            this.peers = peers;
        }

        public List<PeerId> getOldPeers() {
            return oldPeers;
        }

        public void setOldPeers(List<PeerId> oldPeers) {
            this.oldPeers = oldPeers;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public static OutLogEntry getInstance(LogEntry entry) {
            OutLogEntry out = new OutLogEntry();
            out.setType(entry.getType());
            out.setId(entry.getId());
            out.setPeers(entry.getPeers());
            out.setOldPeers(entry.getOldPeers());
            if (entry.getData() != null) {
                out.setData(entry.getData().array());
            }
            return out;
        }

        @Override
        public String toString() {
            return "OutLogEntry{" +
                    "type=" + type +
                    ", id=" + id +
                    '}';
        }
    }

    public static final class AppendEntriesRequest implements Message {
        private String groupId;
        private String serverId;
        private String peerId;
        private Long term;
        private Long prevLogTerm;
        private Long preLogIndex;
        @Deprecated
        private List<LogEntry> entries;
        private List<OutLogEntry> outEntries;
        private Long committedIndex;

        @Override
        public String toString() {
            return "AppendEntriesRequest{" +
                    "groupId='" + groupId + '\'' +
                    ", serverId='" + serverId + '\'' +
                    ", peerId='" + peerId + '\'' +
                    ", term=" + term +
                    ", prevLogTerm=" + prevLogTerm +
                    ", preLogIndex=" + preLogIndex +
                    ", outEntries=" + outEntries +
                    ", committedIndex=" + committedIndex +
                    '}';
        }

        public List<OutLogEntry> getOutEntries() {
            return outEntries;
        }

        public void setOutEntries(List<OutLogEntry> outEntries) {
            this.outEntries = outEntries;
        }

        public List<LogEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<LogEntry> entries) {
            this.entries = entries;
        }

        public int getEntriesCount() {
            if (outEntries == null) {
                return 0;
            }
            return outEntries.size();
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

        @Override
        public String seviceName() {
            return AppendEntriesRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "appendEntries";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public static final class AppendEntriesResponse implements Message {
        private long term;
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

        public long getTerm() {
            return term;
        }

        public void setTerm(long term) {
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
        private long term;
        private Boolean granted;
        private Boolean preVote;

        public Boolean getGranted() {
            return granted;
        }

        public void setGranted(Boolean granted) {
            this.granted = granted;
        }

        public long getTerm() {
            return term;
        }

        public void setTerm(long term) {
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
                    ", granted=" + granted +
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
            if (preVote)
                return "preVote";
            return "requestVote";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public final static class ReadIndexRequest implements Message {
        private String groupId;
        private String serverId;
        private String peerId;
        private List<byte[]> datas;

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
        }

        public void setPeerId(String peerId) {
            this.peerId = peerId;
        }

        public List<byte[]> getDatas() {
            return datas;
        }

        public void setDatas(List<byte[]> datas) {
            this.datas = datas;
        }

        @Override
        public String seviceName() {
            return ReadIndexRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "readIndex";
        }

        @Override
        public String getName() {
            return getClass().getName();
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
        public String toString() {
            return "ReadIndexRequest{" +
                    "groupId='" + groupId + '\'' +
                    ", serverId='" + serverId + '\'' +
                    ", peerId='" + peerId + '\'' +
                    ", datas=" + datas +
                    '}';
        }
    }

    public final static class ReadIndexResponse implements Message {
        private long index;
        private boolean success;

        public long getIndex() {
            return index;
        }

        public void setIndex(long index) {
            this.index = index;
        }

        public boolean getSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        @Override
        public String toString() {
            return "ReadIndexResponse{" +
                    "index=" + index +
                    ", success=" + success +
                    '}';
        }
    }

    public final static class InstallSnapshotRequest implements Message {
        private String groupId;
        private String serverId;
        private String peerId;
        private long term;
        private SnapshotMeta meta;
        private String uri;

        @Override
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

        public long getTerm() {
            return term;
        }

        public void setTerm(long term) {
            this.term = term;
        }

        public SnapshotMeta getMeta() {
            return meta;
        }

        public void setMeta(SnapshotMeta meta) {
            this.meta = meta;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return "InstallSnapshotRequest{" +
                    "groupId='" + groupId + '\'' +
                    ", serverId='" + serverId + '\'' +
                    ", peerId='" + peerId + '\'' +
                    ", term=" + term +
                    ", meta=" + meta +
                    ", uri='" + uri + '\'' +
                    '}';
        }

        @Override
        public String seviceName() {
            return InstallSnapshotRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "intallSnapshot";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }
    }

    public final static class InstallSnapshotResponse implements Message {
        private long term;
        private boolean success;

        public long getTerm() {
            return term;
        }

        public void setTerm(long term) {
            this.term = term;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        @Override
        public String toString() {
            return "InstallSnapshotResponse{" +
                    "term=" + term +
                    ", success=" + success +
                    '}';
        }
    }

    public final static class GetFileRequest implements Message {
        private long readerId;
        private String filename;
        private long count;
        private long offset;
        private boolean readPartly;

        @Override
        public String toString() {
            return "GetFileRequest{" +
                    "readerId=" + readerId +
                    ", filename='" + filename + '\'' +
                    ", count=" + count +
                    ", offset=" + offset +
                    ", readPartly=" + readPartly +
                    '}';
        }

        @Override
        public String seviceName() {
            return GetFileRpc.class.getSimpleName();
        }

        @Override
        public String method() {
            return "getFile";
        }

        @Override
        public String getName() {
            return getClass().getName();
        }

        public long getReaderId() {
            return readerId;
        }

        public void setReaderId(long readerId) {
            this.readerId = readerId;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public boolean isReadPartly() {
            return readPartly;
        }

        public void setReadPartly(boolean readPartly) {
            this.readPartly = readPartly;
        }
    }

    public final static class GetFileResponse implements Message {
        private boolean eof;
        private byte[] data;
        private long readSize;

        public boolean getEof() {
            return eof;
        }

        public void setEof(boolean eof) {
            this.eof = eof;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public long getReadSize() {
            return readSize;
        }

        public void setReadSize(long readSize) {
            this.readSize = readSize;
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

    public final static class Response<T extends Message> implements Message {
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
