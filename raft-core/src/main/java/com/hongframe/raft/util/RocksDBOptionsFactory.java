package com.hongframe.raft.util;

import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-25 14:01
 */
public class RocksDBOptionsFactory {

    static {
        RocksDB.loadLibrary();
    }

    private static final Map<String, DBOptions> rocksDBOptionsTable = new ConcurrentHashMap<>();
    private static final Map<String, ColumnFamilyOptions> columnFamilyOptionsTable = new ConcurrentHashMap<>();
    private static final Map<String, BlockBasedTableConfig> tableFormatConfigTable = new ConcurrentHashMap<>();

    public static DBOptions getRocksDBOptions(final Class<?> cls) {
        DBOptions opts = rocksDBOptionsTable.get(cls.getName());
        if (opts == null) {
            final DBOptions newOpts = getDefaultRocksDBOptions();
            opts = rocksDBOptionsTable.putIfAbsent(cls.getName(), newOpts);
            if (opts == null) {
                opts = newOpts;
            } else {
                newOpts.close();
            }
        }
        // NOTE: This does a shallow copy, which means env, rate_limiter,
        // sst_file_manager, info_log and other pointers will be cloned!
        if(opts.isOwningHandle()) {
            return new DBOptions(opts);
        }
        return null;

    }

    public static DBOptions getDefaultRocksDBOptions() {
        final DBOptions opts = new DBOptions();
        // DB不存在则创建
        opts.setCreateIfMissing(true);
        // 列族不存在则创建
        opts.setCreateMissingColumnFamilies(true);

        // 不限制打开的文件数
        opts.setMaxOpenFiles(-1);

        // max_background_jobs = max_background_compactions + max_background_flushes
        opts.setMaxBackgroundCompactions(Math.min(Utils.CPUS, 4));
        opts.setMaxBackgroundFlushes(1);

        return opts;
    }

    public static ColumnFamilyOptions getRocksDBColumnFamilyOptions(final Class<?> cls) {
        ColumnFamilyOptions opts = columnFamilyOptionsTable.get(cls.getName());
        if (opts == null) {
            final ColumnFamilyOptions newOpts = getDefaultRocksDBColumnFamilyOptions();
            opts = columnFamilyOptionsTable.putIfAbsent(cls.getName(), newOpts);
            if (opts == null) {
                opts = newOpts;
            } else {
                newOpts.close();
            }
        }
        return new ColumnFamilyOptions(opts);
    }

    public static ColumnFamilyOptions getDefaultRocksDBColumnFamilyOptions() {
        final ColumnFamilyOptions opts = new ColumnFamilyOptions();
        // 每个memtable的大小
        opts.setWriteBufferSize(64 * SizeUnit.MB);

        // memtable最大数量
        opts.setMaxWriteBufferNumber(3);

        // 合并memtable -> level0
        opts.setMinWriteBufferNumberToMerge(1);

        // level0文件达到10个。触发压缩至level1
        opts.setLevel0FileNumCompactionTrigger(10);

        // level0文件达到20个，减缓写入
        opts.setLevel0SlowdownWritesTrigger(20);

        // level0文件达到40个，停止写入
        opts.setLevel0StopWritesTrigger(40);

        // 设定level1最大为512MB，后续的等级大小是之前等级的10倍(max_bytes_for_level_multiplier默认设置)
        opts.setMaxBytesForLevelBase(512 * SizeUnit.MB);

        // 每个level1文件为64MB，max_bytes_for_level_base/8，由于target_file_size_multiplier=1，所以后续每个level的文件大小都相等
        // 意味着level1...levelN，需要打开的文件为8*(10^N - 1)/7，target_file_size_base越大，打开的文件越少
        opts.setTargetFileSizeBase(64 * SizeUnit.MB);

        opts.setMemtablePrefixBloomSizeRatio(0.125);

        // Seems like the rocksDB jni for Windows doesn't come linked with any of the
        // compression type
        if (!Platform.isWindows()) {
            opts.setCompressionType(CompressionType.LZ4_COMPRESSION) //
                    .setCompactionStyle(CompactionStyle.LEVEL) //
                    .optimizeLevelStyleCompaction();
        }

        opts.setForceConsistencyChecks(true);

        return opts;
    }

    public static BlockBasedTableConfig getRocksDBTableFormatConfig(final Class<?> cls) {
        BlockBasedTableConfig cfg = tableFormatConfigTable.get(cls.getName());
        if (cfg == null) {
            final BlockBasedTableConfig newCfg = getDefaultRocksDBTableConfig();
            cfg = tableFormatConfigTable.putIfAbsent(cls.getName(), newCfg);
            if (cfg == null) {
                cfg = newCfg;
            }
        }
        return cfg;
    }

    public static BlockBasedTableConfig getDefaultRocksDBTableConfig() {
        // See https://github.com/sofastack/sofa-jraft/pull/156
        return new BlockBasedTableConfig() //
                // Begin to use partitioned index filters
                // https://github.com/facebook/rocksdb/wiki/Partitioned-Index-Filters#how-to-use-it
                .setIndexType(IndexType.kTwoLevelIndexSearch) //
                .setFilterPolicy(new BloomFilter(16, false)) //
                .setPartitionFilters(true) //
                .setMetadataBlockSize(8 * SizeUnit.KB) //
                .setCacheIndexAndFilterBlocks(false) //
                .setCacheIndexAndFilterBlocksWithHighPriority(true) //
                .setPinL0FilterAndIndexBlocksInCache(true) //
                // End of partitioned index filters settings.
                .setBlockSize(4 * SizeUnit.KB)//
                .setBlockCache(new LRUCache(512 * SizeUnit.MB, 8));
    }

}
