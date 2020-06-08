package com.hongframe.raft.slime.options;

import com.hongframe.raft.util.Bytes;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-08 16:53
 */
public class RegionRouteTableOptions {

    private long regionId;
    private String startKey;
    private byte[] startKeyBytes;
    private String endKey;
    private byte[] endKeyBytes;

    public long getRegionId() {
        return regionId;
    }

    public void setRegionId(long regionId) {
        this.regionId = regionId;
    }

    public String getStartKey() {
        return startKey;
    }

    public void setStartKey(String startKey) {
        this.startKey = startKey;
        this.startKeyBytes = Bytes.writeUtf8(startKey);
    }

    public String getEndKey() {
        return endKey;
    }

    public void setEndKey(String endKey) {
        this.endKey = endKey;
        this.endKeyBytes = Bytes.writeUtf8(endKey);
    }

    @Override
    public String toString() {
        return "RegionRouteTableOptions{" +
                "regionId=" + regionId +
                ", startKey='" + startKey + '\'' +
                ", startKeyBytes=" + Bytes.toHex(startKeyBytes) +
                ", endKey='" + endKey + '\'' +
                ", endKeyBytes=" + Bytes.toHex(endKeyBytes) +
                '}';
    }
}
