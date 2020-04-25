package com.hongframe.raft.storage.impl;

import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.codec.LogEntryDecoder;
import com.hongframe.raft.entity.codec.LogEntryEncoder;
import com.hongframe.raft.storage.LogStorage;
import org.rocksdb.DBOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-25 01:32
 */
public class RocksDBLogStorage implements LogStorage {

    private static final Logger LOG = LoggerFactory.getLogger(RocksDBLogStorage.class);

    static {
        RocksDB.loadLibrary();
    }

    private final String path;
    private RocksDB db;
    private DBOptions dbOptions;
    private WriteOptions writeOptions;
    private ReadOptions readOptions;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private final Lock writeLock = this.readWriteLock.writeLock();

    private LogEntryDecoder decoder;
    private LogEntryEncoder encoder;

    public RocksDBLogStorage(String path) {
        this.path = path;
    }

    @Override
    public long getFirstLogIndex() {
        return 0;
    }

    @Override
    public long getLastLogIndex() {
        return 0;
    }

    @Override
    public LogEntry getEntry(long index) {
        return null;
    }

    @Override
    public long getTerm(long index) {
        return 0;
    }

    @Override
    public boolean appendEntry(LogEntry entry) {
        return false;
    }

    @Override
    public int appendEntries(List<LogEntry> entries) {
        return 0;
    }
}
