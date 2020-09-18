/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.network;

import com.ea.eadp.harmony.utils.Helper;
import org.junit.Assert;
import org.junit.Test;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by VincentZhang on 2/12/2018.
 */
public class NetworkTest {
    @Test
    public void testAutoDecideNetworkInterface() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface nic = networkInterfaces.nextElement();
            if(nic.isLoopback()){
                continue;
            }

            List<InterfaceAddress> addresses = nic.getInterfaceAddresses();
            for (InterfaceAddress address : addresses){
                if(address.getBroadcast() == null){
                    continue;
                }

                InetAddress ipAddr = address.getAddress();

                byte[] oct = ipAddr.getAddress();
                oct[3] = 1;
                InetAddress netVIP = InetAddress.getByAddress(oct);

                String writerItf = Helper.getWriterItfv4(netVIP.getHostAddress(), "em0");

                Assert.assertEquals(nic.getName(), writerItf);
            }
        }
    }
}
