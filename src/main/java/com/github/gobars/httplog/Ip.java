package com.github.gobars.httplog;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 基于当前机器IPv4最后8bit的workerId.
 *
 * <p>根据机器IP获取工作进程Id,如果线上机器的IP二进制表示的最后8位不重复,建议使用此种方式,
 *
 * <p>列如机器的IP为192.168.1.108 ,设置workerId为108. 更多见： https://www.jianshu.com/p/7f0661ddd6dd
 */
@Slf4j
public class Ip {
    public static final String LOCAL_IP = init();

    private static String init() {
        try {
            val addr = Ip.getLocalHostLANAddress();
            val localIP = addr.getHostAddress();
            byte[] bytes = addr.getAddress();
            val workerId = bytes[bytes.length - 1] & 0xff;
            return localIP;
        } catch (Exception ex) {
            log.warn("failed to determine LAN address", ex);
        }

        return "unknown";
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN
     * IP address.
     *
     * <p>This method is intended for use as a replacement of JDK method <code>
     * InetAddress.getLocalHost</code>, because that method is ambiguous on Linux systems. Linux
     * systems enumerate the loopback network interface the same way as regular LAN network
     * interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not specify the
     * algorithm used to select the address returned under such circumstances, and will often return
     * the loopback address, which is not valid for network communication. Details <a
     * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     *
     * <p>This method will scan all IP addresses on all network interfaces on the host machine to
     * determine the IP address most likely to be the machine's LAN address. If the machine has
     * multiple IP addresses, this method will prefer a site-local IP address (e.g. 192.168.x.x or
     * 10.10.x.x, usually IPv4) if the machine has one (and will return the first site-local address
     * if the machine has more than one), but if the machine does not hold a site-local address, this
     * method will return simply the first non-loopback address found (IPv4 or IPv6).
     *
     * <p>If this method cannot find a non-loopback address using this selection algorithm, it will
     * fall back to calling and returning the result of JDK method <code>InetAddress.getLocalHost
     * </code>.
     *
     * <p>https://stackoverflow.com/a/20418809
     *
     * @return InetAddress
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        InetAddress candidateAddress = null;

        try {
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                 ifaces.hasMoreElements(); ) {
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAddrs = ifaces.nextElement().getInetAddresses();
                     inetAddrs.hasMoreElements(); ) {
                    val inetAddr = inetAddrs.nextElement();
                    if (inetAddr.isLoopbackAddress()) {
                        continue;
                    }

                    // Found non-loopback site-local address. Return it immediately...
                    if (inetAddr.isSiteLocalAddress()) {
                        return inetAddr;
                    }

                    // Found non-loopback address, but not necessarily site-local.
                    // Store it as a candidate if site-local address is not subsequently found...
                    if (candidateAddress == null) {
                        candidateAddress = inetAddr;
                    }
                }
            }

            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }

            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            val jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("InetAddress.getLocalHost() unexpectedly returned null.");
            }

            return jdkSuppliedAddress;
        } catch (Exception e) {
            val ex = new UnknownHostException("Failed to determine LAN address: " + e);
            ex.initCause(e);
            throw ex;
        }
    }

}