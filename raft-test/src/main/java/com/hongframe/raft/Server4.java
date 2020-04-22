package com.hongframe.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server4 {

    private static final Logger LOG = LoggerFactory.getLogger(Server4.class);

    public static void main(String[] args) {
        RaftServerStartup.startup(8891, RaftServerStartup.NODES);
        LOG.info("server4 started...");
    }

}
