package com.hongframe.raft.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server4 {

    private static final Logger LOG = LoggerFactory.getLogger(Server4.class);

    public static void main(String[] args) {
        CounterRaftServerStartup.startup(8891, CounterRaftServerStartup.NODES);
        LOG.info("server4 started...");
    }

}
