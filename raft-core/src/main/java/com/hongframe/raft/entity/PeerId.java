package com.hongframe.raft.entity;

import com.hongframe.raft.core.ElectionPriority;
import com.hongframe.raft.util.Endpoint;
import com.hongframe.raft.util.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:29
 */
public class PeerId {

    private Endpoint endpoint = new Endpoint(Utils.IP_ANY, 0);

    private int idx;
    private String str;
    private int priority;

    public static PeerId emptyPeer() {
        return new PeerId();
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public PeerId(){}

    public PeerId(final Endpoint endpoint, final int idx) {

        this.endpoint = endpoint;
        this.idx = idx;
    }

    public PeerId(final String ip, final int port) {
        this(ip, port, 0);
    }

    public PeerId(final String ip, final int port, final int idx) {
        this.endpoint = new Endpoint(ip, port);
        this.idx = idx;
    }

    public boolean parse(final String s) {
        if (StringUtils.isEmpty(s)) {
            return false;
        }

        final String[] tmps = StringUtils.splitPreserveAllTokens(s, ':');
        if (tmps.length < 2 || tmps.length > 4) {
            return false;
        }
        try {
            final int port = Integer.parseInt(tmps[1]);
            this.endpoint = new Endpoint(tmps[0], port);

            switch (tmps.length) {
                case 3:
                    this.idx = Integer.parseInt(tmps[2]);
                    break;
                case 4:
                    if (tmps[2].equals("")) {
                        this.idx = 0;
                    } else {
                        this.idx = Integer.parseInt(tmps[2]);
                    }
                    this.priority = Integer.parseInt(tmps[3]);
                    break;
                default:
                    break;
            }
            this.str = null;
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public String getIp() {
        return this.endpoint.getIp();
    }

    public int getPort() {
        return this.endpoint.getPort();
    }

    public boolean isEmpty() {
        return getIp().equals(Utils.IP_ANY) && getPort() == 0 && this.idx == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.endpoint == null ? 0 : this.endpoint.hashCode());
        result = prime * result + this.idx;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerId other = (PeerId) obj;
        if (this.endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else if (!this.endpoint.equals(other.endpoint)) {
            return false;
        }
        return this.idx == other.idx;
    }

    @Override
    public String toString() {
        if (this.str == null) {
            final StringBuilder buf = new StringBuilder(this.endpoint.toString());

            if (this.idx != 0) {
                buf.append(':').append(this.idx);
            }

            if (this.priority != ElectionPriority.Disabled) {
                if (this.idx == 0) {
                    buf.append(':');
                }
                buf.append(':').append(this.priority);
            }

            this.str = buf.toString();
        }
        return this.str;
    }
}
