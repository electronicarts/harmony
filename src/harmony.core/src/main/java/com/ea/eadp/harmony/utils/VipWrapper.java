/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.utils;

import com.ea.eadp.harmony.config.VipServiceConfig;
import com.ea.eadp.harmony.shared.utils.ShellWrapper;
import com.ea.eadp.harmony.shared.utils.ShellWrapper.ExitRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by juding on 10/24/2014.
 */
public class VipWrapper {
    private final static Logger logger = LoggerFactory.getLogger(VipWrapper.class);

    private final static int vipPort = 3306;
    private final static int blkPort = -1;
    private final static boolean doIptablesEnabled = false;
    private final static String UNKNOWN = "_unknown";

    private String writerItf;
    private String writerVip;
    private String readerItf;
    private String readerVip;
    private int servicePort;

    private int writerPfx;
    private String writerMsk;
    private int readerPfx;
    private String readerMsk;

    private String baseDir;

    private boolean autoEthDiscovery = true;

    private String getWriterItfv4(String writerVip, String writerItf) {
        if (autoEthDiscovery) {
            return Helper.getWriterItfv4(writerVip, writerItf);
        }
        return writerItf;
    }

    public VipWrapper(String writerItf, String writerVip, String readerItf, String readerVip, int servicePort, boolean autoEthDiscovery) {
        this.autoEthDiscovery = autoEthDiscovery;
        this.writerVip = writerVip;
        this.servicePort = servicePort;

        this.writerItf = getWriterItfv4(writerVip, writerItf);
        this.writerPfx = getNetworkPrefix(this.writerItf);
        this.writerMsk = getNetworkMask(writerPfx);

        if (readerVip.equals(UNKNOWN)) {
            this.readerVip = null;
            this.readerItf = null;
            this.readerPfx = -1;
            this.readerMsk = null;
        } else {
            this.readerVip = readerVip;
            this.readerItf = getWriterItfv4(readerVip, readerItf);
            this.readerPfx = getNetworkPrefix(this.readerItf);
            this.readerMsk = getNetworkMask(readerPfx);
        }
        this.baseDir = System.getProperty("user.dir");

    }

    public VipWrapper(VipServiceConfig config) {
        this(config.getWriterInterface(),
                config.getWriterVip(),
                config.getReaderInterface(),
                config.getReaderVip(),
                config.getPort(),
                config.getAutoEthDiscovery());
    }

    public ExitRecord addVip() {
        return addVip(false);
    }

    public ExitRecord addVip(boolean doIptables) {
        return addVip(doIptables, true);
    }

    public ExitRecord addVip(boolean doIptables, boolean isWriter) {
        String vip;
        String itf;
        int pfx;
        if (isWriter) {
            vip = writerVip;
            itf = writerItf;
            pfx = writerPfx;
        } else {
            if (readerVip == null) {
                logger.error("Reader VIP not configured");
                return null;
            }
            vip = readerVip;
            itf = readerItf;
            pfx = readerPfx;
        }
        logger.info("Begin to add VIP.");
        doIptables = doIptables && doIptablesEnabled;

        if (doIptables) {
            logger.info("Begin to update IP tables.");
            updateIptables(vip, vipPort, blkPort, servicePort);
        }

        logger.info("Begin to show ip addr on dev:" + itf + ".");
        // Make this method rerunnable.
        ExitRecord res = executeCmd("ip addr show dev %s", itf);
        if (res.failure()) {
            logger.info("show ip address failed. Details:" + res);
            return res;
        }

        logger.info("Begin to add ip addr on dev:" + itf + ". Prefix:" + pfx);
        if (!res.outStr.contains(String.format(" %s/%d ", vip, pfx))) {
            res = executeCmd(
                    "ip addr add dev %s local %s/%d broadcast +",
                    itf, vip, pfx);
            if (res.failure())
                logger.info("Add ip address failed. Details:" + res);
            return res;
        } else {
            logger.warn("Dev:" + itf + " doesn't contain vip, skip add Vip command." + vip);
        }

        /*
        res = executeCmd(
                "%s/bin/config_vip %s %s %s",
                baseDir, itf, vip , writerMsk);
        if (res.failure())
            return res;
        */
        logger.info("Begin to run apring");
        res = executeCmd("arping -A -c 3 -I %s %s", itf, vip);
        logger.info("End of running apring");
        if (res.failure()) {
            logger.info("Arping command failed. Details:" + res);
        }
        logger.info("End of add Vip");
        return res;
    }

    public ExitRecord delVip() {
        return delVip(false);
    }

    public ExitRecord delVip(boolean doIptables) {
        return delVip(doIptables, true);
    }

    public ExitRecord delVip(boolean doIptables, boolean isWriter) {
        String vip;
        String itf;
        if (isWriter) {
            vip = writerVip;
            itf = writerItf;
        } else {
            if (readerVip == null) {
                logger.error("Reader VIP not configured");
                return null;
            }
            vip = readerVip;
            itf = readerItf;
        }
        doIptables = doIptables && doIptablesEnabled;

        logger.info("Begin to show ip addr on : " + itf);
        // Make this method rerunnable.
        ExitRecord res = executeCmd("ip addr show dev %s", itf);
        if (res.failure()) {
            logger.info("show ip address failed. Details:" + res);
            return res;
        }

        logger.info("Begin to list vip prefix on: " + itf);
        Set<Integer> prfxList = getVipPrefixList(itf, vip);
        for (Integer currPrfx : prfxList) {

            logger.info("Begin to delete vip" + vip + " prefix:" + currPrfx + " from: " + itf);
            res = executeCmd(
                    "ip addr del dev %s local %s/%d broadcast +",
                    itf, vip, currPrfx);
            if (res.failure()) {
                logger.info("Del ip address failed. Details:" + res);
                return res;
            }
        }

        /*
        res = executeCmd(
                "%s/bin/config_vip %s -d",
                baseDir, itf);
        */

        if (doIptables) {
            logger.info("Begin to update iptables");
            updateIptables(itf, vipPort, servicePort, blkPort);
        }
        logger.info("End of del Vip");

        return res;
    }

    private static ExitRecord executeCmd(String cmd, Object... args) {
        return ShellWrapper.executeCmd(cmd, args);
    }

    private static Set<Integer> getVipPrefixList(String nifName, String vipAddr) {
        List<InterfaceAddress> addrs;
        try {
            addrs = NetworkInterface.getByName(nifName).getInterfaceAddresses();
        } catch (SocketException ex) {
            logger.error("Failed to get addresses for interface", ex);
            return null;
        }
        Set<Integer> retPrfxList = new HashSet<Integer>();
        for (InterfaceAddress addr : addrs) {
            InetAddress inetAddr = addr.getAddress();
            if (inetAddr instanceof Inet4Address &&
                    inetAddr.getHostAddress().equals(vipAddr)) {
                int currPrfx = addr.getNetworkPrefixLength();
                retPrfxList.add(currPrfx);
            }
        }
        return retPrfxList;
    }

    private static int getNetworkPrefix(String nifName) {
        List<InterfaceAddress> addrs;
        try {
            addrs = NetworkInterface.getByName(nifName).getInterfaceAddresses();
        } catch (SocketException ex) {
            logger.error("Failed to get addresses for interface", ex);
            return -1;
        }
        int retPrfx = 32 + 1;
        for (InterfaceAddress addr : addrs) {
            InetAddress inetAddr = addr.getAddress();
            if (inetAddr instanceof Inet4Address) {
                int currPrfx = addr.getNetworkPrefixLength();
                if (retPrfx > currPrfx)
                    retPrfx = currPrfx;
            }
        }
        if (retPrfx > 32) {
            logger.error("Failed to find address for interface");
            return -1;
        }
        return retPrfx;
    }

    private static String getNetworkMask(int prfx) {
        int pfx = prfx;
        String res = "";
        for (int i = 1; i <= 4; i++) {
            int cut = (pfx >= 8) ? 8 : pfx;
            int seg = 255 & (255 << (8 - cut));
            res += seg + ".";
            pfx -= cut;
        }
        return res.substring(0, res.length() - 1);
    }

    private static ExitRecord updateIptables(String writerVip, int vipPort, int delPort, int addPort) {
        ExitRecord res = null;
        int succCnt = 0;

        if (delPort >= 0) {
            res = executeCmd(
                    "iptables -t nat -C PREROUTING -m tcp -p tcp --dport %d -d %s -j REDIRECT --to-ports %d",
                    vipPort, writerVip, delPort);
            if (res.retVal == 2)
                return res;
            if (res.retVal == 0) {
                for (; ; ) {
                    res = executeCmd(
                            "iptables -t nat -D PREROUTING -m tcp -p tcp --dport %d -d %s -j REDIRECT --to-ports %d",
                            vipPort, writerVip, delPort);
                    if (res.success())
                        succCnt++;
                    else
                        break;
                }
            }
        }

        if (addPort >= 0) {
            res = executeCmd(
                    "iptables -t nat -C PREROUTING -m tcp -p tcp --dport %d -d %s -j REDIRECT --to-ports %d",
                    vipPort, writerVip, addPort);
            if (res.retVal == 2)
                return res;
            if (res.retVal != 0) {
                res = executeCmd(
                        "iptables -t nat -A PREROUTING -m tcp -p tcp --dport %d -d %s -j REDIRECT --to-ports %d",
                        vipPort, writerVip, addPort);
                if (res.success())
                    succCnt++;
            }
        }

        if (succCnt > 0)
            res = executeCmd("service iptables save");

        return res;
    }
}
