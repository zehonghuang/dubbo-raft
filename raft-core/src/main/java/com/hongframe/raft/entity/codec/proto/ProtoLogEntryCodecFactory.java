package com.hongframe.raft.entity.codec.proto;

import com.hongframe.raft.entity.codec.LogEntryCodecFactory;
import com.hongframe.raft.entity.codec.LogEntryDecoder;
import com.hongframe.raft.entity.codec.LogEntryEncoder;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-28 19:18
 */
public class ProtoLogEntryCodecFactory implements LogEntryCodecFactory {
    @Override
    public LogEntryEncoder encoder() {
        return ProtoLogEntryEncoder.INSTANCE;
    }

    @Override
    public LogEntryDecoder decoder() {
        return ProtoLogEntryDecoder.INSTANCE;
    }
}
