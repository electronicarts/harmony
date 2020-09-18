/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.ClusterBuilder;
import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.config.VipServiceConfig;
import com.ea.eadp.harmony.control.ServiceNodeMarker;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.event.ClusterCheckEvent;
import com.ea.eadp.harmony.event.EmailCategoryChangedEvent;
import com.ea.eadp.harmony.event.MonitorLeaderChangedEvent;
import com.ea.eadp.harmony.event.ServiceMasterChangedEvent;
import com.ea.eadp.harmony.monitor.ClusterCheckSteps.ClusterHealthCheckChain;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.ea.eadp.harmony.shared.email.EmailConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import com.ea.eadp.harmony.transition.TransitionConductor;
import com.ea.eadp.harmony.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: leilin
 * Date: 10/8/14
 */
@Component
public class ClusterMonitorServiceImpl extends ServiceSupport implements ClusterMonitorService {
    private final static Logger logger = LoggerFactory.getLogger(ClusterMonitorServiceImpl.class);

    @Autowired
    private NodeMonitor nodeMonitor;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private AutoFailover autoFailover;

    @Autowired
    private TransitionConductor transitionConductor;

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private ClusterBuilder clusterBuilder;

    @Autowired
    private ClusterHealthCheckChain clusterHealthCheckChain;

    private ConcurrentHashMap<String, Boolean> clustersToMonitor = new ConcurrentHashMap<String, Boolean>();

    private final static String UNKNOWN = "_unknown";

    @Override
    public void onMonitorLeaderChanged(MonitorLeaderChangedEvent event) {
        clustersToMonitor.put(event.getClusterId(), event.isLeader());
    }

    @Override
    public void onServiceMasterChanged(ServiceMasterChangedEvent event) {
        String useMasterWatch = clusterManager.getUseMasterWatch();
        logger.info("useMasterWatch is " + useMasterWatch);
        if (useMasterWatch.equals("enabled"))
            transitionConductor.ensureMaster(event.getService(), event.getNode());
    }

    @Override
    public void onEmailCategoryChanged(EmailCategoryChangedEvent event) {
        EmailCategory currentCategory = clusterManager.getEmailEnabledCategory();
        logger.info("email enabled category changed to " + currentCategory);
        if (currentCategory != null)
            emailConfig.setEnabledCategory(currentCategory);
    }

    @Override
    public void onClusterCheck(ClusterCheckEvent event) {
        // Ensure the path first
        String harmonyClusterCheckTimePath = clusterManager.getHarmonyClusterCheckTimePath();
        updateAndCheckTime(harmonyClusterCheckTimePath);

        String harmonyWriterVipStatusPath = clusterManager.getHarmonyWriterVipStatusPath();
        String harmonyReaderVipStatusPath = clusterManager.getHarmonyReaderVipStatusPath();
        List<BaseServiceConfig> targetConfigs = serviceConfigRepository.getInspectionTargets();
        for (BaseServiceConfig baseServiceConfig : targetConfigs) {
            VipServiceConfig config = (VipServiceConfig) baseServiceConfig;
            String writerVip = config.getWriterVip();
            String readerVip = config.getReaderVip();
            String serviceName = config.getService();

            String serviceHrmonyWriterVipStatusPath = harmonyWriterVipStatusPath + "/" + serviceName;
            getZooKeeperService().ensurePath(serviceHrmonyWriterVipStatusPath);

            if (Helper.vipExist(writerVip)) {
                getZooKeeperService().setNodeStringData(serviceHrmonyWriterVipStatusPath, "true");
            } else {
                getZooKeeperService().setNodeStringData(serviceHrmonyWriterVipStatusPath, "false");
            }

            String serviceHrmonyReaderVipStatusPath = harmonyReaderVipStatusPath + "/" + serviceName;
            getZooKeeperService().ensurePath(serviceHrmonyReaderVipStatusPath);

            if (!readerVip.equals(UNKNOWN) && Helper.vipExist(readerVip)) {
                getZooKeeperService().setNodeStringData(serviceHrmonyReaderVipStatusPath, "true");
            } else {
                getZooKeeperService().setNodeStringData(serviceHrmonyReaderVipStatusPath, "false");
            }
        }

        for (Map.Entry<String, Boolean> entry : clustersToMonitor.entrySet()) {
            if (entry.getValue()) {
                checkClusterHealth(entry.getKey());
            }
        }
    }

    public void checkClusterHealth(String clusterId) {
        logger.info("checking health for " + clusterId);

        NodeCheckContext.put("cluster", clusterId);
        clusterHealthCheckChain.handle();

        for (ServiceConfig targetConfig : serviceConfigRepository.getInspectionTargets()) {
            String service = targetConfig.getService();
            String node = targetConfig.getNode();

            // read status
            ServiceNodeStatus status = clusterManager.getServiceNodeStatus(service, node);

            switch (status) {
                case ONLINE:
                    // fall through
                case DOWN:
                    // monitor
                    MonitorResult monitorResult = nodeMonitor.monitorNode(targetConfig);
                    // write status
                    if (monitorResult != null) {
                        ServiceNodeStatus newStatus = monitorResult.getStatus();
                        if (newStatus != status) {
                            String path = clusterManager.getStatusPath(service, node);
                            ZooKeeperService zkSvc = getZooKeeperService();
                            zkSvc.setNodeStringData(path, newStatus.name());

                            // Send email
                            EmailCategory emailCategory;
                            ServiceNodeMarker markerEnm;
                            switch (newStatus) {
                                case ONLINE:
                                    emailCategory = EmailCategory.INFO;
                                    markerEnm = ServiceNodeMarker.GENERIC_INF_SVR;
                                    break;
                                case DOWN:
                                    emailCategory = EmailCategory.ERROR;
                                    markerEnm = ServiceNodeMarker.GENERIC_ERR_SVR;
                                    String markerPath = clusterManager.getMarkerPath(service, node);
                                    String markerStr = zkSvc.getNodeStringData(markerPath);
                                    if (markerStr != null)
                                        markerEnm = ServiceNodeMarker.valueOf(markerStr);
                                    break;
                                default:
                                    emailCategory = EmailCategory.WARN;
                                    markerEnm = ServiceNodeMarker.GENERIC_ERR_SVR;
                            }
                            Map dataModel = new HashMap();
                            dataModel.put("old_status", status);
                            dataModel.put("new_status", newStatus);
                            dataModel.put("root_cause", markerEnm.getDetail());
                            dataModel.put("action", markerEnm.getAction());
                            dataModel.put("hostname", getHostName(service, node));

                            getEmailSender().postEmail(emailCategory, this.getClass(), "NodeStatusChanged",
                                    service + "/" + node + " " + newStatus.name(),
                                    dataModel);
                        }
                    }
                    break;
                default:
                    logger.info("Skipped monitor /{}/{} status {}",
                            new Object[]{service, node, status});
            }
        }

        for (String service : serviceConfigRepository.getServiceList()) {
            autoFailover.detectAutoFailover(service);
        }
    }

    private String getHostName(String service, String node) {
        ServiceConfig serviceConfig = serviceConfigRepository.getServiceConfig(service, node);
        String hostName = null;
        if (serviceConfig instanceof BaseServiceConfig) {
            hostName = ((BaseServiceConfig) serviceConfig).getHost();
        }
        return hostName;
    }
}
