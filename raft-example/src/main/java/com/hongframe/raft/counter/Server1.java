package com.hongframe.raft.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 21:41
 */
public class Server1 {

    private static final Logger LOG = LoggerFactory.getLogger(Server1.class);

    public static void main(String[] args) {
        CounterRaftServerStartup.create(8888, CounterRaftServerStartup.NODES);
        LOG.info("server1 started...");
    }

}
