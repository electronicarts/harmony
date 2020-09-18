/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.monitor;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.ServiceProperties;
import com.ea.eadp.harmony.config.AutoFailoverMode;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
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

import java.util.*;

/**
 * Created by juding on 11/19/2014.
 */
@Component
public class MySqlAutoFailover extends ServiceSupport implements AutoFailover {
    private final static Logger logger = LoggerFactory.getLogger(MySqlAutoFailover.class);

    @Autowired
    private HarmonyEnvironment environment;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private TransitionConductor transitionConductor;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Override
    public void detectAutoFailover(String service) {
        detectMasterAutoFailover(service);
        detectReaderAutoFailover(service);
    }

    private void detectReaderAutoFailover(String service) {
        List<String> nodes = new LinkedList<>();
        nodes.add(clusterManager.getCurrentMaster(service));
        nodes.add(clusterManager.getCurrentPrimarySlave(service));

        if (nodes.contains(null)) {
            logger.warn("Auto failover reader target not found");
            return;
        }

        boolean masterVipStatus = clusterManager.getNodeReaderVipStatus(service, nodes.get(0));
        boolean primaryVipStatus = clusterManager.getNodeReaderVipStatus(service, nodes.get(1));
        if (masterVipStatus == primaryVipStatus) {
            if (!masterVipStatus) {
                logger.error("Reader VIP not configured");
                return;
            }
            logger.error("Detected reader VIP conflict on master and slave");
            return;
        }
        int readerIndex = masterVipStatus ? 0 : 1;
        String readerNode = nodes.get(readerIndex);
        String targetNode = nodes.get((readerIndex + 1) % 2);

        String logPfx = "Auto failover reader " + nodes.get(readerIndex);
        ServiceNodeStatus nodeStatus = clusterManager.getServiceNodeStatus(service, readerNode);
        if (nodeStatus != ServiceNodeStatus.DOWN) {
            logger.warn(logPfx + " " + nodeStatus);
            return;
        }

        ServiceProperties serviceProperties = clusterManager.getAutoFailoverConfig(service);

        long autoFailoverQuota = Long.parseLong(serviceProperties.autoFailoverQuotaReader);
        if (autoFailoverQuota <= 0) {
            logger.warn(logPfx + " no quota");
            return;
        }

        long statusUpdateZxid = clusterManager.getNodeStatusUpdateZxid(service, readerNode);
        long autoFailoverTrigger = Long.parseLong(serviceProperties.autoFailoverTriggerReader);
        if (statusUpdateZxid <= autoFailoverTrigger) {
            logger.warn(logPfx + " already triggered: " + statusUpdateZxid + " " + autoFailoverTrigger);
            return;
        }

        long currTime = clusterManager.getZooKeeperTime(service);

        long statusUpdateTime = clusterManager.getNodeStatusUpdateTime(service, readerNode);
        long autoFailoverGrace = Long.parseLong(serviceProperties.autoFailoverGrace);
        if (currTime - statusUpdateTime < autoFailoverGrace) {
            logger.warn(logPfx + " in grace " + autoFailoverGrace + ": " + statusUpdateTime + " " + currTime);
            return;
        }

        ServiceNodeStatus targetNodeStatus = clusterManager.getServiceNodeStatus(service, targetNode);
        if (targetNodeStatus == ServiceNodeStatus.DOWN) {
            logger.warn(logPfx + " no target");
            return;
        }

        String modeStr = serviceProperties.autoFailoverMode;
        AutoFailoverMode autoFailoverMode = Enum.valueOf(AutoFailoverMode.class, modeStr);
        Map dataObjectMapping = new HashMap<>();

        boolean failoverSucceeded = false;
        try {
            switch (autoFailoverMode) {
                case enabled:
                    logger.warn(logPfx + " to " + targetNode + " now");
                    transitionConductor.moveReader(service, targetNode);
                    failoverSucceeded = true;
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
            clusterManager.setAutoFailoverQuota(service, autoFailoverQuota - 1, false);
            clusterManager.setAutoFailoverTrigger(service, statusUpdateZxid, false);
        } catch (Exception e) {
            dataObjectMapping.put("error", e.getMessage());
        } finally {

            dataObjectMapping.put("autoFailoverMode", autoFailoverMode);
            dataObjectMapping.put("from", readerNode);
            dataObjectMapping.put("to", targetNode);
            dataObjectMapping.put("config", serviceConfigRepository.getServiceConfig(service));

            String rootCause = "Performed auto failover because previous reader node is down!";
            String action = "Check log to find out why previous reader node is down.";
            if (!failoverSucceeded) {
                rootCause = "Error happened while trying to perform auto failover for reader VIP!";
                action = "Check the error message and perform action accordingly.";
            }
            dataObjectMapping.put("action", action);
            dataObjectMapping.put("root_cause", rootCause);

            getEmailSender().postEmail(EmailCategory.ERROR, this.getClass(), "MySQLAutoFailover",
                    service + " auto failover in mode " + autoFailoverMode.name()
                            + " from " + readerNode + " to " + targetNode,
                    dataObjectMapping);
        }
    }

    private void detectMasterAutoFailover(String service) {
        String currMaster = clusterManager.getCurrentMaster(service);
        if (currMaster == null) {
            logger.warn("Auto failover master not found");
            return;
        }

        String logPfx = "Auto failover master " + currMaster;

        ServiceNodeStatus nodeStatus = clusterManager.getServiceNodeStatus(service, currMaster);
        if (nodeStatus != ServiceNodeStatus.DOWN) {
            logger.warn(logPfx + " " + nodeStatus);
            return;
        }

        ServiceProperties serviceProperties = clusterManager.getAutoFailoverConfig(service);

        long autoFailoverQuota = Long.parseLong(serviceProperties.autoFailoverQuota);
        if (autoFailoverQuota <= 0) {
            logger.warn(logPfx + " no quota");
            return;
        }

        long statusUpdateZxid = clusterManager.getNodeStatusUpdateZxid(service, currMaster);
        long autoFailoverTrigger = Long.parseLong(serviceProperties.autoFailoverTrigger);
        if (statusUpdateZxid <= autoFailoverTrigger) {
            logger.warn(logPfx + " already triggered: " + statusUpdateZxid + " " + autoFailoverTrigger);
            return;
        }

        long currTime = clusterManager.getZooKeeperTime(service);

        long statusUpdateTime = clusterManager.getNodeStatusUpdateTime(service, currMaster);
        long autoFailoverGrace = Long.parseLong(serviceProperties.autoFailoverGrace);
        if (currTime - statusUpdateTime < autoFailoverGrace) {
            logger.warn(logPfx + " in grace " + autoFailoverGrace + ": " + statusUpdateTime + " " + currTime);
            return;
        }

        long autoFailoverFresh = Long.parseLong(serviceProperties.autoFailoverFresh);
        String targetNode = targetAutoFailover(service, autoFailoverFresh);
        if (targetNode == null) {
            logger.warn(logPfx + " no target");
            return;
        }

        String modeStr = serviceProperties.autoFailoverMode;
        AutoFailoverMode autoFailoverMode = Enum.valueOf(AutoFailoverMode.class, modeStr);
        Map dataObjectMapping = new HashMap<>();

        boolean failoverSucceeded = false;
        try {
            switch (autoFailoverMode) {
                case enabled:
                    logger.warn(logPfx + " to " + targetNode + " now");
                    transitionConductor.forceMoveMaster(service, targetNode);
                    failoverSucceeded = true;
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
            clusterManager.setAutoFailoverQuota(service, autoFailoverQuota - 1, true);
            clusterManager.setAutoFailoverTrigger(service, statusUpdateZxid, true);
        } catch (Exception e) {
            dataObjectMapping.put("error", e.getMessage());
        } finally {

            dataObjectMapping.put("autoFailoverMode", autoFailoverMode);
            dataObjectMapping.put("from", currMaster);
            dataObjectMapping.put("to", targetNode);
            dataObjectMapping.put("config", serviceConfigRepository.getServiceConfig(service));

            String rootCause = "Performed auto failover because previous master is down!";
            String action = "Check log to find out why previous master down.";
            if (!failoverSucceeded) {
                rootCause = "Error happened while trying to perform auto failover!";
                action = "Check the error message and perform action accordingly.";
            }
            dataObjectMapping.put("action", action);
            dataObjectMapping.put("root_cause", rootCause);

            getEmailSender().postEmail(EmailCategory.ERROR, this.getClass(), "MySQLAutoFailover",
                    service + " auto failover in mode " + autoFailoverMode.name()
                            + " from " + currMaster + " to " + targetNode,
                    dataObjectMapping);
        }
    }

    private String targetAutoFailover(String service, long freshLimit) {
        String currPrimary = clusterManager.getCurrentPrimarySlave(service);
        String logPfx = "Auto failover primary " + currPrimary;
        if (currPrimary != null) {
            ServiceNodeStatus nodeStatus = clusterManager.getServiceNodeStatus(service, currPrimary);
            if (nodeStatus == ServiceNodeStatus.ONLINE) {
                String atPath = clusterManager.getPropertiesPath(service, currPrimary);
                ZooKeeperService zkSvc = getZooKeeperService();
                long behindMaster = zkSvc.getNodeLongData(atPath + "/secondsBehindMaster") * 1000;
                long zxidOfMaster = zkSvc.getNodeLongData(atPath + "/zxidOfMaster");
                long masterZxid = clusterManager.getMasterZxid(service);
                if (zxidOfMaster == masterZxid) {
                    if (0 <= behindMaster && behindMaster <= freshLimit)
                        return currPrimary;
                    else
                        logger.warn(logPfx + " behind " + behindMaster + " freshLimit " + freshLimit);
                } else {
                    logger.warn(logPfx + " zxid " + zxidOfMaster + " != " + masterZxid);
                }
            }
        }
        return null;
    }
}