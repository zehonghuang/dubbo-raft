package com.hongframe.raft.slime.client;

import com.hongframe.raft.Lifecycle;
import com.hongframe.raft.slime.options.SlimeStoreOptions;

import java.util.concurrent.CompletableFuture;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-09 00:45
 */
public interface SlimeStore extends Lifecycle<SlimeStoreOptions> {

    CompletableFuture<Boolean> put(final byte[] key, final byte[] value);

    CompletableFuture<Boolean> put(final String key, final byte[] value);

    CompletableFuture<byte[]> get(final String key);

    CompletableFuture<byte[]> get(final byte[] key);

    CompletableFuture<Boolean> delete(final byte[] key);

    CompletableFuture<Boolean> delete(final String key);

}
