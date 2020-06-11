package com.hongframe.raft.slime.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-11 16:44
 */
public class NetUtil {
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final String LOCAL_IP_ADDRESS;

    static {
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            localAddress = null;
        }

        if (localAddress != null && isValidAddress(localAddress)) {
            LOCAL_IP_ADDRESS = localAddress.getHostAddress();
        } else {
            LOCAL_IP_ADDRESS = getFirstLocalAddress();
        }
    }

    public static String getLocalAddress() {
        return LOCAL_IP_ADDRESS;
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {

        }
        return null; // never get here
    }

    /**
     * Gets the fully qualified domain name for this IP address.
     * Best effort method, meaning we may not be able to return
     * the FQDN depending on the underlying system configuration.
     *
     * @return the fully qualified domain name for this IP address,
     * or if the operation is not allowed by the security check,
     * the textual representation of the IP address.
     */
    public static String getLocalCanonicalHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException ignored) {
            return getLocalHostName();
        }
    }

    /**
     * Gets the first valid IP in the NIC
     */
    private static String getFirstLocalAddress() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface ni = interfaces.nextElement();
                final Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.getHostAddress().contains(":")) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (final Throwable ignored) {
            // ignored
        }

        return "127.0.0.1";
    }

    private static boolean isValidAddress(final InetAddress address) {
        if (address.isLoopbackAddress()) {
            return false;
        }

        final String name = address.getHostAddress();
        return (name != null && !"0.0.0.0".equals(name) && !"127.0.0.1".equals(name) && IP_PATTERN.matcher(name)
                .matches());
    }

    private NetUtil() {
    }
}
