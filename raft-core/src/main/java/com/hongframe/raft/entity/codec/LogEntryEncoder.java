package com.hongframe.raft.entity.codec;

import com.hongframe.raft.entity.LogEntry;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-25 11:03
 */
public interface LogEntryEncoder {

    byte[] encode(LogEntry log);

}
