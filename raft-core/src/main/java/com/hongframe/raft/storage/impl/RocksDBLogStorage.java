package com.hongframe.raft.storage.impl;

import com.hongframe.raft.conf.ConfigurationManager;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.codec.LogEntryDecoder;
import com.hongframe.raft.entity.codec.LogEntryEncoder;
import com.hongframe.raft.option.LogStorageOptions;
import com.hongframe.raft.storage.LogStorage;
import com.hongframe.raft.util.Bits;
import com.hongframe.raft.util.RocksDBOptionsFactory;
import com.hongframe.raft.util.Utils;
import org.apache.commons.io.FileUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final List<ColumnFamilyOptions> cfOpts = new ArrayList<>();
    private ColumnFamilyHandle confHandle;
    private ColumnFamilyHandle defaultHandle;

    private volatile long firstLogIndex;
    private volatile boolean hasLoadFirstLogIndex;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private final Lock writeLock = this.readWriteLock.writeLock();

    private LogEntryDecoder decoder;
    private LogEntryEncoder encoder;

    public RocksDBLogStorage(String path) {
        this.path = path;
    }

    public static DBOptions createDBOptions() {
        return RocksDBOptionsFactory.getRocksDBOptions(RocksDBLogStorage.class);
    }

    public static ColumnFamilyOptions createColumnFamilyOptions() {
        final BlockBasedTableConfig tConfig = RocksDBOptionsFactory
                .getRocksDBTableFormatConfig(RocksDBLogStorage.class);
        return RocksDBOptionsFactory.getRocksDBColumnFamilyOptions(RocksDBLogStorage.class) //
                .useFixedLengthPrefixExtractor(8) //
                .setTableFormatConfig(tConfig) //
                .setMergeOperator(new StringAppendOperator());
    }

    @Override
    public boolean init(LogStorageOptions opts) {
        this.writeLock.lock();
        try {
            if (this.db != null) {
                return true;
            }
            this.dbOptions = createDBOptions();

            this.writeOptions = new WriteOptions();
            this.writeOptions.setSync(true);
            this.readOptions = new ReadOptions();
            this.readOptions.setTotalOrderSeek(true);

            this.encoder = opts.getCodecFactory().encoder();
            this.decoder = opts.getCodecFactory().decoder();
            initAndLoad(opts.getConfigurationManager());
        } catch (Exception e) {
            LOG.error("", e);
            return false;
        } finally {
            this.writeLock.unlock();
        }
        return false;
    }

    private boolean initAndLoad(final ConfigurationManager confManager) throws RocksDBException {
        this.firstLogIndex = 1;
        List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();
        ColumnFamilyOptions cfopts = createColumnFamilyOptions();
        this.cfOpts.add(cfopts);
        descriptors.add(new ColumnFamilyDescriptor("Conf".getBytes(), cfopts));
        descriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfopts));
        openDB(descriptors);
        load(confManager);
        return true;
    }

    private void openDB(final List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

        final File dir = new File(this.path);
        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalStateException("Invalid log path, it's a regular file: " + this.path);
        }
        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
        this.db = RocksDB.open(this.dbOptions, this.path, columnFamilyDescriptors, columnFamilyHandles);

        assert (columnFamilyHandles.size() == 2);
        this.confHandle = columnFamilyHandles.get(0);
        this.defaultHandle = columnFamilyHandles.get(1);
    }

    public static final byte[] FIRST_LOG_IDX_KEY = "meta/firstLogIndex".getBytes(StandardCharsets.UTF_8);

    private void load(final ConfigurationManager confManager) {
        RocksIterator iterator = this.db.newIterator(this.confHandle, this.readOptions);
        while (iterator.isValid()) {
            //TODO Load Configuration
            final byte[] ks = iterator.key();
            final byte[] bs = iterator.value();

            if (Arrays.equals(FIRST_LOG_IDX_KEY, ks)) {
                setFirstLogIndex(Bits.getLong(bs, 0));
//                truncatePrefixInBackground(0L, this.firstLogIndex);
            }
            iterator.next();
        }
    }

    @Override
    public void shutdown() {
        this.writeLock.lock();
        try {
            this.db.close();
            this.confHandle.close();
            this.defaultHandle.close();
            for (ColumnFamilyOptions options : this.cfOpts) {
                options.close();
            }
            this.dbOptions.close();
            this.writeOptions.close();
            this.readOptions.close();
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public long getFirstLogIndex() {
        this.readLock.lock();
        try {
            if (this.hasLoadFirstLogIndex) {
                return this.firstLogIndex;
            }
            RocksIterator iterator = this.db.newIterator(this.defaultHandle, this.readOptions);
            iterator.seekToFirst();
            if (iterator.isValid()) {
                long index = Bits.getLong(iterator.key(), 0);
                setFirstLogIndex(index);
                saveFirstLogIndex(index);
                return index;
            }
        } finally {
            this.readLock.unlock();
        }
        return 0;
    }

    private void setFirstLogIndex(final long index) {
        this.firstLogIndex = index;
        this.hasLoadFirstLogIndex = true; //TODO Conf配置用的
    }

    private boolean saveFirstLogIndex(final long firstLogIndex) {
        this.readLock.lock();
        try {
            final byte[] vs = new byte[8];
            Bits.putLong(vs, 0, firstLogIndex);
            this.db.put(this.confHandle, this.writeOptions, FIRST_LOG_IDX_KEY, vs);
            return true;
        } catch (final RocksDBException e) {
            return false;
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public long getLastLogIndex() {
        this.readLock.lock();
        try {
            RocksIterator iterator = this.db.newIterator(this.defaultHandle, this.readOptions);
            iterator.seekToLast();
            if (iterator.isValid()) {
                return Bits.getLong(iterator.key(), 0);
            }
        } finally {
            this.readLock.unlock();
        }
        return 0;
    }

    @Override
    public LogEntry getEntry(long index) {
        this.readLock.lock();
        try {
            if (index < this.firstLogIndex) {
                return null;
            }
            byte[] k = getKeyBytes(index);
            byte[] v = this.db.get(this.defaultHandle, k);
            if (v != null) {
                LogEntry entry = this.decoder.decode(v);
                if (entry == null) {
                    LOG.warn("log entry is null");
                    return null;
                }
                return entry;
            }
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            this.readLock.unlock();
        }
        return null;
    }

    @Override
    public long getTerm(long index) {
        return 0;
    }

    @Override
    public boolean appendEntry(LogEntry entry) {
        if (entry.getType() == EntryType.ENTRY_TYPE_CONFIGURATION) {
            return false;//TODO Configuration Entry
        } else {
            this.readLock.lock();
            try {
                if (this.db == null) {
                    return false;
                }
                long logIndex = entry.getId().getIndex();
                byte[] k = getKeyBytes(logIndex);
                byte[] v = this.encoder.encode(entry);
                this.db.put(this.defaultHandle, this.writeOptions, k, v);
                return true;
            } catch (Exception e) {
                LOG.error("", e);
            } finally {
                this.readLock.unlock();
            }
        }
        return false;
    }

    @Override
    public int appendEntries(List<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        int len = entries.size();
        this.readLock.lock();
        try {
            WriteBatch batch = new WriteBatch();
            for (int i = 0; i < len; i++) {
                final LogEntry entry = entries.get(i);
                long logIndex = entry.getId().getIndex();
                byte[] k = getKeyBytes(logIndex);
                byte[] v = this.encoder.encode(entry);
                batch.put(this.defaultHandle, k, v);
                LOG.info("batch log entry {index : {}}", entry.getId().toString());
            }
            this.db.write(this.writeOptions, batch);
        } catch (final RocksDBException e) {
            LOG.error("Execute batch failed with rocksdb exception.", e);
            return 0;
        } finally {
            this.readLock.unlock();
        }
        return len;
    }

    @Override
    public boolean truncatePrefix(long firstIndexKept) {
        this.readLock.lock();
        try {
            final long startIndex = getFirstLogIndex();
            final boolean ret = saveFirstLogIndex(firstIndexKept);
            if (ret) {
                setFirstLogIndex(firstIndexKept);
            }
            truncatePrefixInBackground(startIndex, firstIndexKept);
            return ret;
        } finally {
            this.readLock.unlock();
        }
    }

    private void truncatePrefixInBackground(final long startIndex, final long firstIndexKept) {
        Utils.runInThread(() -> {
            this.readLock.lock();
            try {
                if (this.db == null) {
                    return;
                }
                this.db.deleteRange(this.defaultHandle, getKeyBytes(startIndex), getKeyBytes(firstIndexKept));
//                this.db.deleteRange(this.confHandle, getKeyBytes(startIndex), getKeyBytes(firstIndexKept));
            } catch (final RocksDBException e) {
                LOG.error("Fail to truncatePrefix {}.", firstIndexKept, e);
            } finally {
                this.readLock.unlock();
            }
        });
    }

    protected byte[] getKeyBytes(final long index) {
        final byte[] ks = new byte[8];
        Bits.putLong(ks, 0, index);
        return ks;
    }
}
