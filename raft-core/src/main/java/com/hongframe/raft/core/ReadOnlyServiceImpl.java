package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.ReadOnlyService;
import com.hongframe.raft.callback.ReadIndexCallback;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.ReadOnlyServiceOptions;
import com.hongframe.raft.rpc.RpcRequests;
import com.hongframe.raft.util.Bytes;
import com.hongframe.raft.util.DisruptorBuilder;
import com.hongframe.raft.util.LogExceptionHandler;
import com.hongframe.raft.util.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

        this.node.handleReadIndexRequest(request, null);//TODO
    }

    private class ReadIndexState {
        long index = -1;
        Bytes requestContext;
        ReadIndexCallback done;

        public ReadIndexState(Bytes requestContext, ReadIndexCallback done) {
            this.requestContext = requestContext;
            this.done = done;
        }
    }

    private class ReadIndexStatus {
        RpcRequests.ReadIndexRequest request;
        List<ReadIndexState> states;
        long index;
    }

    @Override
    public void shutdown() {

    }
}
