package com.hongframe.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server5 {

    private static final Logger LOG = LoggerFactory.getLogger(Server5.class);

    public static void main(String[] args) {
        RaftServerStartup.startup(8892, RaftServerStartup.NODES);
        LOG.info("server5 started...");
    }

}
