package com.hongframe.raft.slime.client.cmd.store;

import com.hongframe.raft.slime.metadata.RegionEpoch;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-10 17:56
 */
public abstract class BaseRequest implements Serializable {

    public static final byte PUT = 0x01;
    public static final byte DELETE = 0x05;
    public static final byte GET = 0x08;

    private long regionId;
    private RegionEpoch epoch;

    public long getRegionId() {
        return regionId;
    }

    public void setRegionId(long regionId) {
        this.regionId = regionId;
    }

    public RegionEpoch getEpoch() {
        return epoch;
    }

    public void setEpoch(RegionEpoch epoch) {
        this.epoch = epoch;
    }

    public abstract byte magic();

    @Override
    public String toString() {
        return "BaseRequest{" +
                "regionId=" + regionId +
                ", epoch=" + epoch +
                '}';
    }
}
