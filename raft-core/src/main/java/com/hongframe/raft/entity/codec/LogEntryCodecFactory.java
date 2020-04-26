package com.hongframe.raft.entity.codec;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-26 14:23
 */
public interface LogEntryCodecFactory {

    LogEntryEncoder encoder();

    LogEntryDecoder decoder();

}
