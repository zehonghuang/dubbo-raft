package com.hongframe.raft.slime.metadata;

import com.hongframe.raft.util.Copiable;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-09 01:09
 */
public class RegionEpoch implements Copiable<RegionEpoch>, Comparable<RegionEpoch>, Serializable {

    private long confVersion;

    private long version;

    public RegionEpoch() {
    }

    public RegionEpoch(long confVersion, long version) {
        this.confVersion = confVersion;
        this.version = version;
    }

    public long getConfVersion() {
        return confVersion;
    }

    public void setConfVersion(long confVersion) {
        this.confVersion = confVersion;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RegionEpoch that = (RegionEpoch) o;
        return confVersion == that.confVersion && version == that.version;
    }

    @Override
    public RegionEpoch copy() {
        return new RegionEpoch(this.confVersion, this.version);
    }

    @Override
    public int compareTo(RegionEpoch o) {
        if (this.version == o.version) {
            return (int) (this.confVersion - o.confVersion);
        }
        return (int) (this.version - o.version);
    }
}
