package com.hongframe.raft;

import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.rpc.ClientRequests.*;
import com.hongframe.raft.rpc.ClientService;
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

    public boolean updateLeader(final String groupId, final String leaderStr) {
        final PeerId leader = new PeerId();
        if (leader.parse(leaderStr)) {
            return updateLeader(groupId, leader);
        } else {
            return false;
        }
    }

    public boolean updateLeader(final String groupId, final PeerId leader) {
        final GroupConf gc = getGroupConf(groupId);
        final StampedLock stampedLock = gc.stampedLock;
        final long stamp = stampedLock.writeLock();
        try {
            gc.leader = leader;
        } finally {
            stampedLock.unlockWrite(stamp);
        }
        return true;
    }

    public PeerId selectLeader(final String groupId) {
        final GroupConf gc = this.groupConfTable.get(groupId);
        if (gc == null) {
            return null;
        }
        final StampedLock stampedLock = gc.stampedLock;
        long stamp = stampedLock.tryOptimisticRead();
        PeerId leader = gc.leader;
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                leader = gc.leader;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return leader;
    }

    public Configuration getConf(final String groupId) {
        final GroupConf gc = this.groupConfTable.get(groupId);
        if (gc == null) {
            return null;
        }
        final StampedLock stampedLock = gc.stampedLock;
        long stamp = stampedLock.tryOptimisticRead();
        Configuration conf = gc.conf;
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                conf = gc.conf;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return conf;
    }

    public Status refreshLeader(final ClientService clientService, final String groupId) {
        final Configuration conf = getConf(groupId);
        if(conf == null) {
            return new Status(10001, "");
        }

        GetLeaderRequest request = new GetLeaderRequest();
        request.setGroupId(groupId);

        for(PeerId peerId : conf.getPeers()) {
            if(!clientService.connect(peerId)) {
                return new Status(10001, "");
            }
            Message message = clientService.getLeader(peerId, request);
            if(message instanceof GetLeaderResponse) {
                GetLeaderResponse response = (GetLeaderResponse) message;
                String leaderStr = response.getLeaderId();
                updateLeader(groupId, leaderStr);
                return Status.OK();
            } else {
                return new Status(10001, "");
            }
        }
        return new Status(10001, "");
    }

    private static class GroupConf {
        private final StampedLock stampedLock = new StampedLock();
        private Configuration conf;
        private PeerId leader;
    }

}
