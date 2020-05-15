package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.StateMachine;
import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.CallbackQueue;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.option.FSMCallerOptions;
import com.hongframe.raft.storage.LogManager;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 20:04
 */
public class FSMCallerImpl implements FSMCaller {

    private static final Logger LOG = LoggerFactory.getLogger(FSMCallerImpl.class);

    private StateMachine stateMachine;
    private LogManager logManager;
    private NodeImpl node;
    private final AtomicLong applyingIndex;
    private long lastAppliedTerm;
    private final AtomicLong lastAppliedIndex;
    private CallbackQueue callbackQueue;
    private Disruptor<CallerTask> disruptor;
    private RingBuffer<CallerTask> taskQueue;
    private final CopyOnWriteArrayList<LastAppliedLogIndexListener> lastAppliedLogIndexListeners = new CopyOnWriteArrayList<>();

    public FSMCallerImpl() {
        super();
        this.lastAppliedIndex = new AtomicLong(0);
        this.applyingIndex = new AtomicLong(0);
    }

    private enum TaskType {
        IDLE,
        COMMITTED,
        ERROR,
        ;
    }

    private class CallerTask {
        TaskType type;
        long committedIndex;
        long term;

        public void reset() {
            this.type = null;
            this.committedIndex = 0;
            this.term = 0;
        }
    }

    private class CallerTaskFactory implements EventFactory<CallerTask> {

        @Override
        public CallerTask newInstance() {
            return new CallerTask();
        }
    }

    private class CallerTaskHandler implements EventHandler<CallerTask> {
        private long maxCommittedIndex = -1;

        @Override
        public void onEvent(CallerTask event, long sequence, boolean endOfBatch) throws Exception {
            this.maxCommittedIndex = runApplyTask(event, this.maxCommittedIndex, endOfBatch);
        }
    }

    @Override
    public boolean init(FSMCallerOptions opts) {
        this.stateMachine = opts.getFsm();
        this.logManager = opts.getLogManager();
        this.node = opts.getNode();
        this.callbackQueue = opts.getCallbackQueue();

        this.disruptor = DisruptorBuilder.<CallerTask>newInstance() //
                .setEventFactory(new CallerTaskFactory()) //
                .setRingBufferSize(opts.getDisruptorBufferSize()) //
                .setThreadFactory(new NamedThreadFactory("Dubbo-Raft-FSMCaller-Disruptor-", true)) //
                .setProducerType(ProducerType.MULTI) //
                .setWaitStrategy(new BlockingWaitStrategy()) //
                .build();
        this.disruptor.handleEventsWith(new CallerTaskHandler());
        this.disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.taskQueue = this.disruptor.start();
        return true;
    }

    @Override
    public boolean onCommitted(long committedIndex) {
        LOG.info("committed index: {}", committedIndex);
        EventTranslator<CallerTask> tpl = (task, sequence) -> {
            task.committedIndex = committedIndex;
            task.type = TaskType.COMMITTED;
        };
        if (!this.taskQueue.tryPublishEvent(tpl)) {
            return false;
        }
        return true;
    }

    private long runApplyTask(final CallerTask task, long maxCommittedIndex, final boolean endOfBatch) {
        LOG.info("max commited index: {}, end of batch:{}", maxCommittedIndex, endOfBatch);
        if (task.type == TaskType.COMMITTED) {
            if (task.committedIndex > maxCommittedIndex) {
                maxCommittedIndex = task.committedIndex;
            }
        }
        if (maxCommittedIndex > -1 && endOfBatch) {
            doCommitted(maxCommittedIndex);
            maxCommittedIndex = -1L;
        }
        return maxCommittedIndex;
    }

    private void doCommitted(final long committedIndex) {
        final List<Callback> callbacks = new ArrayList<>();
        long firstIndex = this.callbackQueue.popClosureUntil(committedIndex, callbacks);
        //TODO doCommitted
        final IteratorImpl iterator = new IteratorImpl(this.stateMachine, this.logManager, callbacks, firstIndex,
                committedIndex, this.applyingIndex, this.lastAppliedIndex.get());
        while (iterator.isGood()) {
            final LogEntry logEntry = iterator.entry();
            if (logEntry.getType() == EntryType.ENTRY_TYPE_CONFIGURATION) {
                //TODO ENTRY_TYPE_CONFIGURATION
            } else {
                doApplyTasks(iterator);
            }
        }
        final long lastIndex = iterator.getIndex() - 1;
        final long lastTerm = this.logManager.getTerm(lastIndex);
        final LogId lastAppliedId = new LogId(lastIndex, lastTerm);
        this.lastAppliedIndex.set(lastIndex);
        this.lastAppliedTerm = lastTerm;
        LOG.info("last applied index: {}, last applied term: {}", this.applyingIndex.get(), this.lastAppliedTerm);
        this.logManager.setAppliedId(lastAppliedId);
    }

    private void doApplyTasks(final IteratorImpl iterator) {
        final IteratorWrapper iter = new IteratorWrapper(iterator);
        this.stateMachine.onApply(iter);
        if (iter.hasNext()) {
            //TODO error
        }
        iter.next();
    }

    private void notifyLastAppliedIndexUpdated(final long lastAppliedIndex) {
        for (final LastAppliedLogIndexListener listener : this.lastAppliedLogIndexListeners) {
            listener.onApplied(lastAppliedIndex);
        }
    }

    @Override
    public boolean onLeaderStop(Status status) {
        return false;
    }

    @Override
    public boolean onLeaderStart(long term) {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
