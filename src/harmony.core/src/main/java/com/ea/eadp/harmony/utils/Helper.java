/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.utils;

import com.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * User: leilin
 * Date: 10/2/14
 */
public class Helper {

    private final static Logger logger = LoggerFactory.getLogger(Helper.class);

    /**
     * @param vip
     * @return whether this VIP exist in all network cards of this server
     */
    public static boolean vipExist(String vip){
        try {
            InetAddress targetIP = InetAddress.getByName(vip);
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface nic = networkInterfaces.nextElement();

                if (!nic.isLoopback()) {
                    List<InterfaceAddress> addressList = nic.getInterfaceAddresses();

                    for (InterfaceAddress address : addressList) {
                        // Per: https://docs.oracle.com/javase/7/docs/api/java/net/InterfaceAddress.html,
                        // IPv6 address will return null for getBroadcast, ignore
                        if(address.getBroadcast() == null)
                            continue;

                        InetAddress ipAddress = address.getAddress();
                        if( ipAddress.equals(targetIP) )
                            return true;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Socket exception happened!", e);
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException exception happened!", e);
        } catch (Exception e) {
            logger.error("Unexpected exception happened!", e);
        }
        return false;
    }

    public static boolean sameNetwork(InetAddress ip1, InetAddress ip2, short prflen)
            throws Exception {

        byte[] a1 = ip1.getAddress();
        byte[] a2 = ip2.getAddress();

        int shft = 0xffffffff << (32 - prflen);
        byte[] oct = new byte[4];
        oct[0] = (byte) (((byte) ((shft & 0xff000000) >> 24)) & 0xff);
        oct[1] = (byte) (((byte) ((shft & 0x00ff0000) >> 16)) & 0xff);
        oct[2] = (byte) (((byte) ((shft & 0x0000ff00) >> 8)) & 0xff);
        oct[3] = (byte) (((byte) (shft & 0x000000ff)) & 0xff);
        byte[] m = InetAddress.getByAddress(oct).getAddress();

        for (int i = 0; i < a1.length; i++)
            if ((a1[i] & m[i]) != (a2[i] & m[i]))
                return false;

        return true;
    }

    /**
     * Find the first network interface (network card) that the VIP can be assigned.
     * If not found, use the default Itf name.
     * Only supports IPv4
     *
     * @param vip        The vip
     * @param defaultItf Default eth name if the VIP can't be added to any network card.
     * @return
     */
    public static String getWriterItfv4(String vip, String defaultItf) {
        try {
            InetAddress targetIP = InetAddress.getByName(vip);

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface nic = networkInterfaces.nextElement();

                if (!nic.isLoopback()) {
                    List<InterfaceAddress> addressList = nic.getInterfaceAddresses();

                    for (InterfaceAddress address : addressList) {
                        // Per: https://docs.oracle.com/javase/7/docs/api/java/net/InterfaceAddress.html,
                        // IPv6 address will return null for getBroadcast, ignore
                        if(address.getBroadcast() == null)
                            continue;

                        InetAddress ipAddress = address.getAddress();
                        Short subnetMask = address.getNetworkPrefixLength();
                        boolean sameVlan = sameNetwork(targetIP, ipAddress, subnetMask);

                        if (sameVlan) {
                            return nic.getName();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            logger.error("Socket exception happened!", e);
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException exception happened!", e);
        } catch (Exception e) {
            logger.error("Unexpected exception happened!", e);
        }
        return defaultItf;
    }

    public static Integer host2Long(String host) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        return InetAddresses.coerceToInteger(address);
    }
}
