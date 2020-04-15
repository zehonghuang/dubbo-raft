package com.hongframe.raft.util;

import java.net.InetSocketAddress;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:30
 */
public class Endpoint implements Copiable<Endpoint> {

    private String            ip               = Utils.IP_ANY;
    private int               port;
    private String            str;

    public Endpoint(){}

    public Endpoint(String address, int port) {
        this.ip = address;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(this.ip, this.port);
    }

    @Override
    public String toString() {
        if (str == null) {
            str = this.ip + ":" + this.port;
        }
        return str;
    }

    @Override
    public Endpoint copy() {
        return null;
    }
}
