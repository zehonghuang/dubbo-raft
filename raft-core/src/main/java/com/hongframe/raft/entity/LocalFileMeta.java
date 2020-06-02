package com.hongframe.raft.entity;

import java.io.Serializable;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-28 21:41
 */
public class LocalFileMeta implements Serializable {

    private String userMeta;
    private int source;
    private boolean hasChecksum;
    private String checksum;

    @Override
    public String toString() {
        return "LocalFileMeta{" +
                "userMeta='" + userMeta + '\'' +
                ", source=" + source +
                ", checksum='" + checksum + '\'' +
                '}';
    }

    public boolean hasChecksum() {
        return this.hasChecksum;
    }
    public void setHasChecksum(boolean hasChecksum) {
        this.hasChecksum = hasChecksum;
    }

    public String getUserMeta() {
        return userMeta;
    }

    public void setUserMeta(String userMeta) {
        this.userMeta = userMeta;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
