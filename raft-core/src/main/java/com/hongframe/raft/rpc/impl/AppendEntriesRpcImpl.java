package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.RequestCallback;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.AppendEntriesRpc;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 16:52
 */
public class AppendEntriesRpcImpl implements AppendEntriesRpc {

    private final ConcurrentMap<String, ConcurrentMap<String, SequenceRequestContext>> seqRequestContexts = new ConcurrentHashMap<>();

    class SequenceMessage implements Comparable<SequenceMessage> {
        private final int sequence;
        private final AsyncContext asyncContext;
        private final Message message;

        public SequenceMessage(int sequence, Message message, AsyncContext asyncContext) {
            this.sequence = sequence;
            this.message = message;
            this.asyncContext = asyncContext;
        }

        public void sendResponse() {
            asyncContext.signalContextSwitch();
            asyncContext.write(message);
        }

        @Override
        public int compareTo(SequenceMessage o) {
            return Integer.compare(this.sequence, o.sequence);
        }
    }

    private class SequenceRequestContext {
        private final String groupId;
        private final String peerId;
        private int sequence;
        private int nextRequiredSequence;
        private final PriorityQueue<SequenceMessage> responseQueue;
        private final static int MAX_PENDING_RESPONSES = 256;

        private SequenceRequestContext(String groupId, String peerId) {
            this.groupId = groupId;
            this.peerId = peerId;
            this.responseQueue = new PriorityQueue<>(50);
        }

        boolean hasTooManyPendingResponses() {
            return this.responseQueue.size() > MAX_PENDING_RESPONSES;
        }

        int getAndIncreSequence() {
            int prev = this.sequence;
            this.sequence++;
            if (this.sequence < 0) {
                this.sequence = 0;
            }
            return prev;
        }

        public int getNextRequiredSequence() {
            return this.nextRequiredSequence;
        }

        int getAndIncreRequiredSequence() {
            int prev = this.nextRequiredSequence;
            this.nextRequiredSequence++;
            if (this.nextRequiredSequence < 0) {
                this.nextRequiredSequence = 0;
            }
            return prev;
        }
    }

    private SequenceRequestContext getSequenceRequestContext(String groupId, String peerId) {
        ConcurrentMap<String, SequenceRequestContext> contextConcurrentMap = this.seqRequestContexts.get(groupId);
        if (contextConcurrentMap == null) {
            contextConcurrentMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, SequenceRequestContext> existsCtxs = this.seqRequestContexts.putIfAbsent(groupId,
                    contextConcurrentMap);
            if (existsCtxs != null) {
                contextConcurrentMap = existsCtxs;
            }
        }
        SequenceRequestContext seqCtx = contextConcurrentMap.get(peerId);
        if (seqCtx == null) {
            synchronized (contextConcurrentMap) {
                seqCtx = contextConcurrentMap.get(peerId);
                if (seqCtx == null) {
                    seqCtx = new SequenceRequestContext(groupId, peerId);
                    contextConcurrentMap.put(peerId, seqCtx);
                }
            }
        }

        return seqCtx;
    }

    private class SequenceRequestCallback implements RequestCallback {
        final String groupId;
        final String peerId;
        final int reqSeq;
        final AsyncContext asyncContext;


        public SequenceRequestCallback(String groupId, String peerId, int reqSeq, AsyncContext asyncContext) {
            this.groupId = groupId;
            this.peerId = peerId;
            this.reqSeq = reqSeq;
            this.asyncContext = asyncContext;
        }

        @Override
        public void run(Status status) {
            if (!status.isOk()) {
                asyncContext.signalContextSwitch();
                asyncContext.write(new Response<>(new ErrorResponse(10001, status.getErrorMsg())));
            }
        }

        @Override
        public void sendResponse(final Message msg) {
            SequenceRequestContext seqCtx = getSequenceRequestContext(this.groupId, this.peerId);
            final PriorityQueue<SequenceMessage> respQueue = seqCtx.responseQueue;
            synchronized (respQueue) {
                respQueue.add(new SequenceMessage(this.reqSeq, msg, asyncContext));

                if (!seqCtx.hasTooManyPendingResponses()) {
                    while (!respQueue.isEmpty()) {
                        final SequenceMessage queuedResponse = respQueue.peek();

                        if (queuedResponse.sequence != seqCtx.getNextRequiredSequence()) {
                            break;
                        }

                        respQueue.remove();
                        try {
                            queuedResponse.sendResponse();
                        } finally {
                            seqCtx.getAndIncreRequiredSequence();
                        }
                    }
                } else {
                    //TODO error
                }
            }
        }
    }

    @Override
    public Response<AppendEntriesResponse> appendEntries(AppendEntriesRequest request) {
        final AsyncContext asyncContext = RpcContext.startAsync();

        SequenceRequestContext context = getSequenceRequestContext(request.getGroupId(), request.getPeerId());
        int seq = context.getAndIncreSequence();

        Message message = getNode(request).handleAppendEntriesRequest(request,
                new SequenceRequestCallback(request.getGroupId(), request.getPeerId(), seq, asyncContext));
        if (message == null) {
            return null;
        }
        asyncContext.stop();
        return checkResponse(message);
    }

}
