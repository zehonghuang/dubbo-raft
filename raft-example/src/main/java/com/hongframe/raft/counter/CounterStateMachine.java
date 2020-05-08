package com.hongframe.raft.counter;

import com.hongframe.raft.Iterator;
import com.hongframe.raft.StateMachine;
import com.hongframe.raft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-08 16:50
 */
public class CounterStateMachine implements StateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(CounterStateMachine.class);

    private final AtomicLong value      = new AtomicLong(0);

    private final AtomicLong    leaderTerm = new AtomicLong(-1);

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    @Override
    public void onApply(Iterator iterator) {
        if(!isLeader()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        CounterCallback callback = null;
        while (iterator.hasNext()) {
            if(iterator.callback() != null) {
                callback = (CounterCallback) iterator.callback();
            }
            final ByteBuffer data = iterator.data();
            value.set(data.getLong());

            if(callback != null) {
                callback.success(value.get());
                callback.run(Status.OK());
            }
            iterator.next();
        }
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void onLeaderStart(long term) {
        LOG.info("onLeaderStart: term={}.", term);
        leaderTerm.set(term);
    }

    @Override
    public void onLeaderStop(Status status) {

    }
}
