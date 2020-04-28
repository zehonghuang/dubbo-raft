package com.hongframe.raft.entity.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hongframe.raft.entity.EntryType;
import com.hongframe.raft.entity.LogEntry;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.entity.codec.LogEntryDecoder;
import com.hongframe.raft.util.AsciiStringUtil;
import com.hongframe.raft.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-28 20:54
 */
public class ProtoLogEntryDecoder implements LogEntryDecoder {

    public static final ProtoLogEntryDecoder INSTANCE = new ProtoLogEntryDecoder();

    private ProtoLogEntryDecoder() {
    }

    @Override
    public LogEntry decode(byte[] bs) {
        try {
            LogOutter.LogEntry lle = LogOutter.LogEntry.parseFrom(bs);

            final LogEntry log = new LogEntry();
            log.setType(EntryType.get(lle.getType()));
            log.getId().setIndex(lle.getIndex());
            log.getId().setTerm(lle.getTerm());

            if (lle.getPeersCount() > 0) {
                final List<PeerId> peers = new ArrayList<>(lle.getPeersCount());
                for (final ByteString bstring : lle.getPeersList()) {
                    peers.add(Utils.getPeerId(AsciiStringUtil.unsafeDecode(bstring)));
                }
                log.setPeers(peers);
            }
            if (lle.getOldPeersCount() > 0) {
                final List<PeerId> peers = new ArrayList<>(lle.getOldPeersCount());
                for (final ByteString bstring : lle.getOldPeersList()) {
                    peers.add(Utils.getPeerId(AsciiStringUtil.unsafeDecode(bstring)));
                }
                log.setOldPeers(peers);
            }

//            if (lle.getLearnersCount() > 0) {
//                final List<PeerId> peers = new ArrayList<>(lle.getLearnersCount());
//                for (final ByteString bstring : lle.getLearnersList()) {
//                    peers.add(JRaftUtils.getPeerId(AsciiStringUtil.unsafeDecode(bstring)));
//                }
//                log.setLearners(peers);
//            }
//
//            if (lle.getOldLearnersCount() > 0) {
//                final List<PeerId> peers = new ArrayList<>(lle.getOldLearnersCount());
//                for (final ByteString bstring : lle.getOldLearnersList()) {
//                    peers.add(JRaftUtils.getPeerId(AsciiStringUtil.unsafeDecode(bstring)));
//                }
//                log.setOldLearners(peers);
//            }

            return log;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
