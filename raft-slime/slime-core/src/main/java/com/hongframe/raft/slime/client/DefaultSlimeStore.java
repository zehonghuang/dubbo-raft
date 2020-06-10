package com.hongframe.raft.slime.client;

import com.hongframe.raft.slime.options.SlimeStoreOptions;

import java.util.concurrent.CompletableFuture;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-09 00:58
 */
public class DefaultSlimeStore implements SlimeStore {
    @Override
    public boolean init(SlimeStoreOptions opts) {
        return false;
    }

    @Override
    public CompletableFuture<Boolean> put(byte[] key, byte[] value) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> put(String key, byte[] value) {
        return null;
    }

    @Override
    public CompletableFuture<byte[]> get(String key) {
        return null;
    }

    @Override
    public CompletableFuture<byte[]> get(byte[] key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(byte[] key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(String key) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
