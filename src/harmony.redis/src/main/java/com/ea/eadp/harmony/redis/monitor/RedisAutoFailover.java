/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.monitor;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.config.AutoFailoverMode;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeMarker;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.monitor.AutoFailover;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import com.ea.eadp.harmony.transition.TransitionConductor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by juding on 5/18/16.
 */
@Component
public class RedisAutoFailover extends ServiceSupport implements AutoFailover {
    private final static Logger logger = LoggerFactory.getLogger(RedisAutoFailover.class);
    private final static String TODO_INFO_AFTER_AUTO_FAILOVER = "Followup: 1. Try to start the redis service;2. Set redis service to slave;3. Set the redis to slave in zookeeper";
    @Autowired
    private HarmonyEnvironment environment;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private TransitionConductor transitionConductor;

    @Override
    public void detectAutoFailover(String service) {
        String currMaster = clusterManager.getCurrentMaster(service);
        if (currMaster == null) {
            logger.error("Auto failover master not found");
            return;
        }

        String logPfx = "Current master " + currMaster;
        logger.info(logPfx);

        ServiceNodeStatus nodeStatus = clusterManager.getServiceNodeStatus(service, currMaster);
        if (nodeStatus != ServiceNodeStatus.DOWN) {
            logger.info(logPfx + " " + nodeStatus);
            return;
        }
        logger.error(logPfx + " " + nodeStatus);

        String atPath = clusterManager.getPropertiesPath(service);
        ZooKeeperService zkSvc = getZooKeeperService();

        long statusUpdateZxid = clusterManager.getNodeStatusUpdateZxid(service, currMaster);
        long autoFailoverTrigger = zkSvc.getNodeLongData(atPath + "/autoFailoverTrigger");
        if (statusUpdateZxid <= autoFailoverTrigger) {
            logger.warn("Failover already triggered: " + statusUpdateZxid + " " + autoFailoverTrigger);
            return;
        }

        long currTime = clusterManager.getZooKeeperTime(service);

        long statusUpdateTime = clusterManager.getNodeStatusUpdateTime(service, currMaster);
        long autoFailoverGrace = zkSvc.getNodeLongData(atPath + "/autoFailoverGrace");
        if (currTime - statusUpdateTime < autoFailoverGrace) {
            logger.warn("Failover in grace " + autoFailoverGrace + ": " + statusUpdateTime + " " + currTime);
            return;
        }

        String modeStr = zkSvc.getNodeStringData(atPath + "/autoFailoverMode");
        AutoFailoverMode autoFailoverMode = Enum.valueOf(AutoFailoverMode.class, modeStr);
        String targetNode = getAutoFailoverTarget(service);

        if (targetNode == null) {
            String errMsg = new String("Primary slave is offline! Auto failover aborted!");
            logger.error(errMsg);
            return;
        }

        boolean failoverCompleted = true;
        switch (autoFailoverMode) {
            case enabled:
                logger.warn(logPfx + " to " + targetNode + " now");
                try {
                    transitionConductor.forceMoveMaster(service, targetNode);
                } catch (RuntimeException e) {
                    failoverCompleted = false;
                }
                break;
            case shadow:
                logger.warn(logPfx + " to " + targetNode + " shadow");
                break;
            case disabled:
                logger.warn(logPfx + " to " + targetNode + " disabled");
                return;
            default:
                logger.warn(logPfx + " to " + targetNode + " unknown mode " + autoFailoverMode);
                return;
        }

        zkSvc.setNodeLongData(atPath + "/autoFailoverTrigger", statusUpdateZxid);

        EmailCategory emailCategory = EmailCategory.ERROR;
        ServiceNodeMarker markerEnm = ServiceNodeMarker.REDIS_ERR_AUTO_FAILOVER;

        Map dataObjectMapping = new HashMap<>();
        dataObjectMapping.put("autoFailoverMode", autoFailoverMode);
        dataObjectMapping.put("from", currMaster);
        dataObjectMapping.put("to", targetNode);
        dataObjectMapping.put("config", serviceConfigRepository.getServiceConfig(service));
        dataObjectMapping.put("action", markerEnm.getAction());
        dataObjectMapping.put("root_cause", markerEnm.getDetail());

        getEmailSender().postEmail(emailCategory, this.getClass(), "RedisAutoFailover",
                service + " auto failover in mode " + autoFailoverMode.name() + " Completed:" + failoverCompleted,
                dataObjectMapping);
    }

    private String getAutoFailoverTarget(String service) {
        String currPrimary = clusterManager.getCurrentPrimarySlave(service);
        if (currPrimary != null) {
            ServiceNodeStatus nodeStatus = clusterManager.getServiceNodeStatus(service, currPrimary);
            if (nodeStatus == ServiceNodeStatus.ONLINE) {
                return currPrimary;
            }
            return null;
        }
        return null;
    }
}
