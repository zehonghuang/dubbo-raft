package com.hongframe.raft.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server3 {

    private static final Logger LOG = LoggerFactory.getLogger(Server3.class);

    public static void main(String[] args) {
        CounterRaftServerStartup.startup(8890, CounterRaftServerStartup.NODES);
        LOG.info("server3 started...");
    }

}
