package com.hongframe.raft.slime.storage;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 10:10
 */
public interface RawKVStore {

    void get(final byte[] key, final KVStoreCallback callback);

    void put(final byte[] key, final byte[] value, final KVStoreCallback callback);

    void delete(final byte[] key, final KVStoreCallback callback);

}
