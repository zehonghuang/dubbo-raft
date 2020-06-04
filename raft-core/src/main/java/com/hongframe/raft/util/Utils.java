package com.hongframe.raft.util;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.entity.PeerId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:42
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static final int CPUS = SystemPropertyUtil.getInt(
            "dubbo.raft.available_processors", Runtime
                    .getRuntime().availableProcessors());

    public static final int MIN_EXECUTOR_POOL_SIZE = SystemPropertyUtil.getInt(
            "dubbo.raft.closure.threadpool.size.min",
            CPUS);

    public static final int MAX_EXECUTOR_POOL_SIZE = SystemPropertyUtil.getInt(
            "dubbo.raft.closure.threadpool.size.max",
            Math.max(100, CPUS * 5));

    public static final int RAFT_DATA_BUF_SIZE = SystemPropertyUtil.getInt(
            "dubbo.raft.byte_buf.size", 1024);


    private static final ThreadPoolExecutor GLOBAL_EXECUTOR = new ThreadPoolExecutor(MIN_EXECUTOR_POOL_SIZE,
            MAX_EXECUTOR_POOL_SIZE, 60l, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NamedThreadFactory("Global-Executor", false));


    public static final String IP_ANY = "0.0.0.0";

    public static long nowMs() {
        return System.currentTimeMillis();
    }

    public static ByteBuffer expandByteBuffer(final ByteBuffer buf) {
        return expandByteBufferAtLeast(buf, RAFT_DATA_BUF_SIZE);
    }

    public static ByteBuffer allocate(final int size) {
        return ByteBuffer.allocate(size);
    }

    public static ByteBuffer allocate() {
        return allocate(RAFT_DATA_BUF_SIZE);
    }

    public static ByteBuffer expandByteBufferAtLeast(final ByteBuffer buf, final int minLength) {
        final int newCapacity = minLength > RAFT_DATA_BUF_SIZE ? minLength : RAFT_DATA_BUF_SIZE;
        final ByteBuffer newBuf = ByteBuffer.allocate(buf.capacity() + newCapacity);
        buf.flip();
        newBuf.put(buf);
        return newBuf;
    }

    public static ByteBuffer expandByteBufferAtMost(final ByteBuffer buf, final int maxLength) {
        final int newCapacity = maxLength > RAFT_DATA_BUF_SIZE || maxLength <= 0 ? RAFT_DATA_BUF_SIZE : maxLength;
        final ByteBuffer newBuf = ByteBuffer.allocate(buf.capacity() + newCapacity);
        buf.flip();
        newBuf.put(buf);
        return newBuf;
    }

    public static Future<?> runInThread(final Runnable runnable) {
        return GLOBAL_EXECUTOR.submit(runnable);
    }

    public static Future<?> runCallbackInThread(final Callback callback) {
        if (callback == null) {
            return null;
        }
        return runCallbackInThread(callback, Status.OK());
    }

    public static Future<?> runCallbackInThread(final Callback callback, final Status status) {
        if (callback == null) {
            return null;
        }
        return runInThread(() -> {
            try {
                callback.run(status);
            } catch (final Throwable t) {

            }
        });
    }

    public static int closeQuietly(final Closeable closeable) {
        if (closeable == null) {
            return 0;
        }
        try {
            closeable.close();
            return 0;
        } catch (final IOException e) {
            LOG.error("Fail to close", e);
            return 10001;
        }
    }

    public static long monotonicMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public static PeerId getPeerId(final String s) {
        final PeerId peer = new PeerId();
        if (StringUtils.isBlank(s)) {
            return peer;
        }
        if (peer.parse(s)) {
            return peer;
        }
        throw new IllegalArgumentException("Invalid peer str:" + s);
    }

    public static boolean atomicMoveFile(final File source, final File target) throws IOException {
        // Move temp file to target path atomically.
        // The code comes from https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/util/AtomicFileWriter.java#L187
        final Path sourcePath = source.toPath();
        final Path targetPath = target.toPath();
        try {
            return Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE) != null;
        } catch (final IOException e) {
            // If it falls here that can mean many things. Either that the atomic move is not supported,
            // or something wrong happened. Anyway, let's try to be over-diagnosing
            if (e instanceof AtomicMoveNotSupportedException) {
                LOG.warn("Atomic move not supported. falling back to non-atomic move, error: {}.", e.getMessage());
            } else {
                LOG.warn("Unable to move atomically, falling back to non-atomic move, error: {}.", e.getMessage());
            }

            if (target.exists()) {
                LOG.info("The target file {} was already existing.", targetPath);
            }

            try {
                return Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING) != null;
            } catch (final IOException e1) {
                e1.addSuppressed(e);
                LOG.warn("Unable to move {} to {}. Attempting to delete {} and abandoning.", sourcePath, targetPath,
                        sourcePath);
                try {
                    Files.deleteIfExists(sourcePath);
                } catch (final IOException e2) {
                    e2.addSuppressed(e1);
                    LOG.warn("Unable to delete {}, good bye then!", sourcePath);
                    throw e2;
                }

                throw e1;
            }
        }
    }


}
