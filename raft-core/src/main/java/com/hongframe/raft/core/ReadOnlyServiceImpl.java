package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.ReadOnlyService;
import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ReadIndexCallback;
import com.hongframe.raft.callback.ResponseCallbackAdapter;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.ReadOnlyServiceOptions;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 18:15
 */
public class ReadOnlyServiceImpl implements ReadOnlyService {

    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyServiceImpl.class);

    private RaftOptions raftOptions;
    private NodeImpl node;
    private final Lock lock = new ReentrantLock();
    private FSMCaller fsmCaller;

    private Disruptor<ReadIndexEvent> readIndexDisruptor;
    private RingBuffer<ReadIndexEvent> readIndexQueue;

    private final TreeMap<Long, List<ReadIndexStatus>> pendingNotifyStatus = new TreeMap<>();

    private static class ReadIndexEvent {
        Bytes bytes;
        ReadIndexCallback callback;
    }

    private static class ReadIndexEventFactory implements EventFactory<ReadIndexEvent> {
        @Override
        public ReadIndexEvent newInstance() {
            return new ReadIndexEvent();
        }
    }

    private class ReadIndexEventHandler implements EventHandler<ReadIndexEvent> {
        private final List<ReadIndexEvent> events = new ArrayList<>(
                ReadOnlyServiceImpl.this.raftOptions.getApplyBatch());

        @Override
        public void onEvent(final ReadIndexEvent newEvent, final long sequence, final boolean endOfBatch)
                throws Exception {
            this.events.add(newEvent);
            if (this.events.size() >= ReadOnlyServiceImpl.this.raftOptions.getApplyBatch() || endOfBatch) {
                executeReadIndexEvents(this.events);
                this.events.clear();
            }
        }
    }

    @Override
    public boolean init(ReadOnlyServiceOptions opts) {
        this.node = opts.getNode();
        this.fsmCaller = opts.getFsmCaller();
        this.raftOptions = opts.getRaftOptions();
        this.fsmCaller.addLastAppliedLogIndexListener(this);
        this.readIndexDisruptor = DisruptorBuilder.<ReadIndexEvent>newInstance() //
                .setEventFactory(new ReadIndexEventFactory()) //
                .setRingBufferSize(this.raftOptions.getDisruptorBufferSize()) //
                .setThreadFactory(new NamedThreadFactory("Dubbo-Raft-ReadOnlyService-Disruptor", true)) //
                .setWaitStrategy(new BlockingWaitStrategy()) //
                .setProducerType(ProducerType.MULTI) //
                .build();
        this.readIndexDisruptor.handleEventsWith(new ReadIndexEventHandler());
        this.readIndexDisruptor
                .setDefaultExceptionHandler(new LogExceptionHandler<Object>(this.getClass().getSimpleName()));
        this.readIndexQueue = this.readIndexDisruptor.start();

        return false;
    }

    @Override
    public void addRequest(byte[] reqCtx, ReadIndexCallback callback) {
        EventTranslator<ReadIndexEvent> translator = (event, sequence) -> {
            event.bytes = new Bytes(reqCtx);
            event.callback = callback;
        };
        while (true) {
            if (this.readIndexQueue.tryPublishEvent(translator)) {
                break;
            } else {
                Thread.yield();
            }
        }
    }

    private void executeReadIndexEvents(final List<ReadIndexEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        RpcRequests.ReadIndexRequest request = new RpcRequests.ReadIndexRequest();
        request.setGroupId(this.node.getNodeId().getGroupId());
        request.setServerId(this.node.getNodeId().getPeerId().toString());

        final List<ReadIndexState> states = new ArrayList<>(events.size());
        List<byte[]> byteses = new ArrayList<>();
        for (final ReadIndexEvent event : events) {
            states.add(new ReadIndexState(event.bytes, event.callback));
            byteses.add(event.bytes.get());
        }
        request.setDatas(byteses);

        this.node.handleReadIndexRequest(request, new ReadIndexResponseCallback(states, request));
    }

    private class ReadIndexResponseCallback extends ResponseCallbackAdapter {
        final List<ReadIndexState> states;
        final RpcRequests.ReadIndexRequest request;

        public ReadIndexResponseCallback(List<ReadIndexState> states, RpcRequests.ReadIndexRequest request) {
            this.states = states;
            this.request = request;
        }

        @Override
        public void run(Status status) {
            if (!status.isOk()) {
                LOG.error("error message: {}", status.getErrorMsg());
                notifyFail(status);
                return;
            }

            final RpcRequests.ReadIndexResponse readIndexResponse = (RpcRequests.ReadIndexResponse) getResponse();
            if (!readIndexResponse.getSuccess()) {
                return;
            }
            for (final ReadIndexState state : this.states) {
                state.index = readIndexResponse.getIndex();
            }
            final ReadIndexStatus readIndexStatus = new ReadIndexStatus(this.request, this.states,
                    readIndexResponse.getIndex());
            boolean doUnlock = true;
            ReadOnlyServiceImpl.this.lock.lock();
            try {
                if (readIndexStatus.isApplied(ReadOnlyServiceImpl.this.fsmCaller.getLastAppliedIndex())) {
                    notifySuccess(readIndexStatus);
                    doUnlock = false;
                    ReadOnlyServiceImpl.this.lock.unlock();
                } else {
                    ReadOnlyServiceImpl.this.pendingNotifyStatus
                            .computeIfAbsent(readIndexStatus.index, k -> new ArrayList<>(10))
                            .add(readIndexStatus);
                }
            } finally {
                if (doUnlock) {
                    ReadOnlyServiceImpl.this.lock.unlock();
                }
            }

        }

        private void notifyFail(final Status status) {
            for (final ReadIndexState state : this.states) {
                final ReadIndexCallback callback = state.callback;
                if (callback != null) {
                    final Bytes reqCtx = state.requestContext;
                    callback.run(status, ReadIndexCallback.INVALID_LOG_INDEX, reqCtx != null ? reqCtx.get() : null);
                }
            }
        }
    }

    private class ReadIndexState {
        long index = -1;
        Bytes requestContext;
        ReadIndexCallback callback;

        public ReadIndexState(Bytes requestContext, ReadIndexCallback done) {
            this.requestContext = requestContext;
            this.callback = done;
        }
    }

    private class ReadIndexStatus {
        RpcRequests.ReadIndexRequest request;
        List<ReadIndexState> states;
        long index;

        public ReadIndexStatus(RpcRequests.ReadIndexRequest request, List<ReadIndexState> states, long index) {
            this.request = request;
            this.states = states;
            this.index = index;
        }

        public boolean isApplied(long appliedIndex) {
            return appliedIndex >= this.index;
        }

    }

    @Override
    public void onApplied(long lastAppliedLogIndex) {
        List<ReadIndexStatus> pendingStatuses = null;
        this.lock.lock();
        try {
            if (this.pendingNotifyStatus.isEmpty()) {
                return;
            }
            final Map<Long, List<ReadIndexStatus>> statuses = this.pendingNotifyStatus.headMap(lastAppliedLogIndex, true);
            if (statuses != null) {
                pendingStatuses = new ArrayList<>(statuses.size() << 1);
                final Iterator<Map.Entry<Long, List<ReadIndexStatus>>> it = statuses.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<Long, List<ReadIndexStatus>> entry = it.next();
                    pendingStatuses.addAll(entry.getValue());
                    it.remove();
                }
            }
        } finally {
            this.lock.unlock();
            if (pendingStatuses != null && !pendingStatuses.isEmpty()) {
                for (final ReadIndexStatus status : pendingStatuses) {
                    notifySuccess(status);
                }
            }
        }
    }

    private void notifySuccess(ReadIndexStatus status) {
        final List<ReadIndexState> states = status.states;
        final int taskCount = states.size();
        for (int i = 0; i < taskCount; i++) {
            final ReadIndexState task = states.get(i);
            final ReadIndexCallback callback = task.callback; // stack copy
            if (callback != null) {
                callback.setResult(task.index, task.requestContext.get());
                callback.run(Status.OK());
            }
        }
    }


    @Override
    public void shutdown() {

    }
}
