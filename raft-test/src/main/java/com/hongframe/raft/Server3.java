package com.hongframe.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server3 {

    private static final Logger LOG = LoggerFactory.getLogger(Server3.class);

    public static void main(String[] args) {
        RaftServerStartup.startup(8890, RaftServerStartup.NODES);
        LOG.info("server3 started...");
    }

}
