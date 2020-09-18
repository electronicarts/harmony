/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.service;

import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.ea.eadp.harmony.shared.email.EmailSender;
import com.ea.eadp.harmony.shared.event.EventService;
import com.ea.eadp.harmony.shared.event.HarmonyEvent;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: leilin
 * Date: 10/8/14
 */
public abstract class ServiceSupport {
    private final static Logger logger = LoggerFactory.getLogger(ServiceSupport.class);
    protected final static Long timeDifferenceWarningMili = 180000L;

    @Autowired
    private ZooKeeperConfig zooKeeperConfig;

    @Autowired
    private EmailSender emailSender;

    // event support
    @Autowired
    private EventService eventService;

    public void rasieEvent(HarmonyEvent event) {
        eventService.raiseEvent(event);
    }

    public void rasieEventAsync(HarmonyEvent event) {
        eventService.raiseEventAsync(event);
    }

    // zk support
    @Autowired
    private ZooKeeperService zooKeeperService;

    public ZooKeeperService getZooKeeperService() {
        return zooKeeperService;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

    public void updateAndCheckTime(String zkprPath) {
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.ensurePath(zkprPath);

        long harmonyCurMillis = System.currentTimeMillis();
        zkSvc.setNodeStringData(zkprPath, Long.toString(harmonyCurMillis));

        // Check if time difference is too big for this zookeeper and this harmony
        Stat stat = zkSvc.checkExists(zkprPath);
        long zkTime = stat.getMtime();

        if (Math.abs(zkTime - harmonyCurMillis) > timeDifferenceWarningMili) {
            try {
                Map dataObjectMapping = new HashMap<>();
                dataObjectMapping.put("zkpr_connection_string", zooKeeperConfig.getConnectionString());
                dataObjectMapping.put("zkpr_path", zkprPath);
                dataObjectMapping.put("harmony_node_address", InetAddress.getLocalHost().getHostName());
                dataObjectMapping.put("root_cause", "Time difference between zkpr and Harmony too large");
                dataObjectMapping.put("action", "Check and adjust time on zkpr and harmony server");
                dataObjectMapping.put("time_difference", Math.abs(harmonyCurMillis - zkTime));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(harmonyCurMillis);
                dataObjectMapping.put("harmony_time", sdf.format(calendar.getTime()));
                calendar.setTimeInMillis(zkTime);
                dataObjectMapping.put("zkpr_time", sdf.format(calendar.getTime()));

                emailSender.postEmail(EmailCategory.WARN, this.getClass(), "LargeTimeDifference",
                        "Time difference too large between Zkpr and Harmony", dataObjectMapping);

            } catch (UnknownHostException e) {
                logger.error("Can't get host name!", e);
            }
        }
    }

    public void onApplicatoinClosed() {
        if (this.emailSender != null && !this.emailSender.isTerminated()) {
            this.emailSender.close();
        }

        if (eventService != null) {
            eventService.close();
        }
    }
}
