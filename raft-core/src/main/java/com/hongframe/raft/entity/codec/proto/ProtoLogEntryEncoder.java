package com.hongframe.raft.entity.codec.proto;

import com.google.protobuf.ByteString;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.LogId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.entity.codec.LogEntryEncoder;
import com.hongframe.raft.util.AsciiStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-28 21:26
 */
public class ProtoLogEntryEncoder implements LogEntryEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(ProtoLogEntryEncoder.class);

    public static final ProtoLogEntryEncoder INSTANCE = new ProtoLogEntryEncoder();

    private ProtoLogEntryEncoder() {
    }

    @Override
    public byte[] encode(LogEntry log) {

        final LogId logId = log.getId();
        final LogOutter.LogEntry.Builder builder = LogOutter.LogEntry.newBuilder() //
                .setType(log.getType().getType()) //
                .setIndex(logId.getIndex()) //
                .setTerm(logId.getTerm());

        final List<PeerId> peers = log.getPeers();
        if (hasPeers(peers)) {
            encodePeers(builder, peers);
        }

        final List<PeerId> oldPeers = log.getOldPeers();
        if (hasPeers(oldPeers)) {
            encodeOldPeers(builder, oldPeers);
        }

        if(log.getData() != null) {
            byte[] bs = log.getData().array();
            builder.setData(ByteString.copyFrom(log.getData()));
            log.setData(ByteBuffer.wrap(bs));
        } else {
            builder.setData(ByteString.EMPTY);
        }

        return builder.build().toByteArray();
    }

    private boolean hasPeers(final Collection<PeerId> peers) {
        return peers != null && !peers.isEmpty();
    }

    private void encodePeers(final LogOutter.LogEntry.Builder builder, final List<PeerId> peers) {
        final int size = peers.size();
        for (int i = 0; i < size; i++) {
            builder.addPeers(ByteString.copyFrom(AsciiStringUtil.unsafeEncode(peers.get(i).toString())));
        }
    }

    private void encodeOldPeers(final LogOutter.LogEntry.Builder builder, final List<PeerId> peers) {
        final int size = peers.size();
        for (int i = 0; i < size; i++) {
            builder.addOldPeers(ByteString.copyFrom(AsciiStringUtil.unsafeEncode(peers.get(i).toString())));
        }
    }
}
