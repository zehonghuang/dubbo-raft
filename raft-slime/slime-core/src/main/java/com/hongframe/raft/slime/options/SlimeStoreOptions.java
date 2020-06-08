package com.hongframe.raft.slime.options;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-08 22:00
 */
public class SlimeStoreOptions {

    private long clusterId;

    private String clusterName;

    private boolean onlyLeaderRead;

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public boolean isOnlyLeaderRead() {
        return onlyLeaderRead;
    }

    public void setOnlyLeaderRead(boolean onlyLeaderRead) {
        this.onlyLeaderRead = onlyLeaderRead;
    }

    @Override
    public String toString() {
        return "SlimeStoreOptions{" +
                "clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", onlyLeaderRead=" + onlyLeaderRead +
                '}';
    }
}
