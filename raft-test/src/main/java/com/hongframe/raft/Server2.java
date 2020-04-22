package com.hongframe.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 21:41
 */
public class Server2 {

    private static final Logger LOG = LoggerFactory.getLogger(Server2.class);

    public static void main(String[] args) {
        RaftServerStartup.startup(8889, RaftServerStartup.NODES);
        LOG.info("server2 started...");
    }
}
