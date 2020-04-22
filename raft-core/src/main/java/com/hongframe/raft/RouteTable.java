package com.hongframe.raft;

import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.entity.PeerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;

public class RouteTable {

    private static final Logger LOG = LoggerFactory.getLogger(RouteTable.class);

    private static final RouteTable INSTANCE = new RouteTable();
    private final ConcurrentMap<String, GroupConf> groupConfTable = new ConcurrentHashMap<>();
    public static RouteTable getInstance() {
        return INSTANCE;
    }

    public boolean updateConf(final String groupId, final String confStr) {

        final Configuration conf = new Configuration();
        if (conf.parse(confStr)) {
            return updateConf(groupId, conf);
        } else {
            return false;
        }
    }

    public boolean updateConf(final String groupId, final Configuration conf) {
        final GroupConf gc = getGroupConf(groupId);
        final StampedLock lock = gc.stampedLock;
        final long stamp = lock.writeLock();
        try {
            gc.conf = conf;
            if(gc.leader != null && !conf.contains(gc.leader)) {
                gc.leader = null;
            }
        } finally {
            lock.unlock(stamp);
        }
        return true;
    }

    private GroupConf getGroupConf(final String groupId) {
        GroupConf gc = this.groupConfTable.get(groupId);
        if(gc == null) {
            gc = new GroupConf();
            final GroupConf old = this.groupConfTable.putIfAbsent(groupId, gc);
            if(old != null) {
                gc = old;
            }
        }
        return gc;
    }

    private static class GroupConf {
        private final StampedLock stampedLock = new StampedLock();
        private Configuration conf;
        private PeerId leader;
    }

}
