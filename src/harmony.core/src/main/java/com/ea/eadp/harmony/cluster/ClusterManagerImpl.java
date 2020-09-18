/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster;

import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ClusterBuilder;
import com.ea.eadp.harmony.cluster.entity.ClusterConfig;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodesStatus;
import com.ea.eadp.harmony.cluster.entity.ServiceProperties;
import com.ea.eadp.harmony.config.AutoFailoverMode;
import com.ea.eadp.harmony.config.AutoFailoverServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperHelper;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * User: leilin
 * Date: 10/7/14
 */
public class ClusterManagerImpl extends ServiceSupport implements ClusterManager {
    @Autowired
    private ZooKeeperConfig zooKeeperConfig;

    @Autowired
    private HarmonyEnvironment environment;

    @Autowired
    protected ServiceConfigRepository serviceConfigRepository;

    @Autowired
    protected ClusterBuilder clusterBuilder;

    private final static Logger logger = LoggerFactory.getLogger(ClusterManagerImpl.class);

    public String getCurrentCluster() {
        return environment.getCluster();
    }

    public String getCurrentNode() {
        return serviceConfigRepository.getCurrentNode();
    }

    public String getWarmUpSwitchPath() {
        return zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + "warmUpSwitch";
    }

    public String getClusterPath() {
        return zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + environment.getClusterType()
                + "/" + environment.getCluster();
    }

    public String getLeaderPath() {
        return getClusterPath() + "/leader";
    }

    public String getRunnerCountPath() {
        return getClusterPath() + "/runnerCount";
    }

    @Override
    public String getServicePath(String service) {
        return getClusterPath() + "/" + service;
    }

    public String getNodePath(String service, String node) {
        return getClusterPath()
                + "/" + service + "/nodes/" + node;
    }

    public String getObservationPath(String service, String subjectNode, String observerNode) {
        return getNodePath(service, subjectNode)
                + "/observations/" + observerNode;
    }

    public String getObservationsPath(String service, String node) {
        return getNodePath(service, node)
                + "/observations";
    }

    public String getStatusPath(String service, String node) {
        return getNodePath(service, node)
                + "/status";
    }

    public String getMarkerPath(String service, String node) {
        return getNodePath(service, node)
                + "/marker";
    }

    // To avoid confliction, add a new node to replace the old marker node.
    public String getMarkerStepPath(String service, String node) {
        return getNodePath(service, node)
                + "/markerStep";
    }

    public String getMasterPath(String service) {
        return getClusterPath()
                + "/" + service + "/roles/master";
    }

    public String getPrimarySlavePath(String service) {
        return getClusterPath()
                + "/" + service + "/roles/primary_slave";
    }

    public String getCurrentMaster(String service) {
        String path = getMasterPath(service);
        ZooKeeperService zkSvc = getZooKeeperService();
        String data = zkSvc.getNodeStringData(path);
        return data;
    }

    public String getCurrentPrimarySlave(String service) {
        String path = getPrimarySlavePath(service);
        ZooKeeperService zkSvc = getZooKeeperService();
        String data = zkSvc.getNodeStringData(path);
        return data;
    }

    public void setCurrentMaster(String service, String value) {
        String path = getMasterPath(service);
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.setNodeStringData(path, value);
    }

    public void setCurrentPrimarySlave(String service, String value) {
        String path = getPrimarySlavePath(service);
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.setNodeStringData(path, value);
    }

    public void initClusterInZooKeeper() {
        int currStage = 1;
        logger.info("initClusterInZooKeeper() " + currStage++);

        String clusterPath = getClusterPath();
        ZooKeeperService zkSvc = getZooKeeperService();

        String rcPath = clusterPath + "/runnerCount";
        zkSvc.ensurePath(rcPath);
        zkSvc.setNodeLongData(rcPath, 0L);

        enhanceClusterPath(clusterPath, zkSvc);

        logger.info("initClusterInZooKeeper() " + currStage++);

        List<String> serviceList = serviceConfigRepository.getServiceList();
        for (String service : serviceList) {
            // Wrote all service names into zookeeper
            String serviceNamePath = clusterPath + "/services";
            zkSvc.ensurePath(serviceNamePath + "/" + service);

            String servicePath = clusterPath + "/" + service;
            zkSvc.ensurePath(servicePath);
            zkSvc.ensurePath(servicePath + "/lock");
            zkSvc.ensurePath(servicePath + "/roles");
            zkSvc.ensurePath(servicePath + "/roles/master");
            zkSvc.ensurePath(servicePath + "/roles/primary_slave");
            zkSvc.ensurePath(servicePath + "/nodes");

            enhanceServicePath(service, servicePath, zkSvc);

            logger.info("initClusterInZooKeeper() " + currStage++);

            List<String> nodeList = serviceConfigRepository.getServiceNodes(service);
            for (String node : nodeList) {
                String nodePath = getNodePath(service, node);
                zkSvc.ensurePath(nodePath);
                zkSvc.ensurePath(nodePath + "/lock");
                zkSvc.ensurePath(nodePath + "/status");
                zkSvc.ensurePath(nodePath + "/observations");
                zkSvc.ensurePath(nodePath + "/links");
                zkSvc.ensurePath(nodePath + "/marker");

                enhanceNodePath(service, node, nodePath, zkSvc);

                logger.info("initClusterInZooKeeper() " + currStage++);
            }
        }
    }

    protected void enhanceClusterPath(String path, ZooKeeperService zkSvc) {
        String atPath = path + "/properties";
        zkSvc.ensurePath(atPath);

        String configPath = path + "/config";
        zkSvc.ensurePath(configPath);

        String valPath = atPath + "/emailEnabledCategory";
        zkSvc.ensurePath(valPath);

        valPath = atPath + "/useMasterWatch";
        zkSvc.ensurePath(valPath);
    }

    protected void enhanceServicePath(String service, String path, ZooKeeperService zkSvc) {
        // do nothing
    }

    protected void enhanceServicePath(String path, ZooKeeperService zkSvc, AutoFailoverServiceConfig config) {
        String atPath = path + "/properties";
        zkSvc.ensurePath(atPath);
        zkSvc.ensurePath(atPath + "/autoFailoverFresh");
        zkSvc.ensurePath(atPath + "/autoFailoverGrace");
        zkSvc.ensurePath(atPath + "/autoFailoverMode");
        zkSvc.ensurePath(atPath + "/autoFailoverQuota");
        zkSvc.ensurePath(atPath + "/autoFailoverTrigger");
        zkSvc.ensurePath(atPath + "/autoFailoverQuotaReader");
        zkSvc.ensurePath(atPath + "/autoFailoverTriggerReader");

        String valPath;

        for (String vPath : new String[]{atPath + "/autoFailoverQuota", atPath + "/autoFailoverQuotaReader"}) {
            if (zkSvc.getNodeStringData(vPath) == null) {
                long value = config.getAutoFailoverMaxQuota();
                zkSvc.setNodeLongData(vPath, value);
            }
        }

        valPath = atPath + "/autoFailoverFresh";
        if (zkSvc.getNodeStringData(valPath) == null) {
            long value = config.getAutoFailoverFresh();
            zkSvc.setNodeLongData(valPath, value);
        }

        valPath = atPath + "/autoFailoverGrace";
        if (zkSvc.getNodeStringData(valPath) == null) {
            long value = config.getAutoFailoverGrace();
            zkSvc.setNodeLongData(valPath, value);
        }

        valPath = atPath + "/autoFailoverMode";
        if (zkSvc.getNodeStringData(valPath) == null) {
            String value = config.getAutoFailoverMode().name();
            zkSvc.setNodeStringData(valPath, value);
        }

        for (String vPath : new String[]{atPath + "/autoFailoverTrigger", atPath + "/autoFailoverTriggerReader"}) {
            if (zkSvc.getNodeStringData(vPath) == null) {
                long value = 0L;
                zkSvc.setNodeLongData(vPath, value);
            }
        }
    }

    protected void enhanceNodePath(String service, String node, String path, ZooKeeperService zkSvc) {
        // do nothing
    }

    private String getTimePath(String service) {
        return getClusterPath()
                + "/" + service + "/clock_time";
    }

    public Long getZooKeeperTime(String service) {
        final String ctPath = getTimePath(service);
        ZooKeeperService zkSvc = getZooKeeperService();
        return zkSvc.execute(new Command<CuratorFramework, Long>() {
            @Override
            public Long execute(CuratorFramework client) {
                ZooKeeperHelper.deleteSubtree(client, ctPath);
                ZooKeeperHelper.ensurePath(client, ctPath);
                Stat stat = ZooKeeperHelper.checkExists(client, ctPath);
                ZooKeeperHelper.deleteSubtree(client, ctPath);
                return stat.getCtime();
            }
        });
    }

    public Long getUpdateZxid(String path) {
        ZooKeeperService zkSvc = getZooKeeperService();
        Stat stat = zkSvc.checkExists(path);
        Long zxid = 0L;
        if (stat != null)
            zxid = stat.getMzxid();
        return zxid;
    }

    public Long getMasterZxid(String service) {
        String path = getMasterPath(service);
        return getUpdateZxid(path);
    }

    public Long getNodeStatusUpdateZxid(String service, String node) {
        String path = getStatusPath(service, node);
        return getUpdateZxid(path);
    }

    public Long getUpdateTime(String path) {
        ZooKeeperService zkSvc = getZooKeeperService();
        Stat stat = zkSvc.checkExists(path);
        Long time = 0L;
        if (stat != null)
            time = stat.getMtime();
        return time;
    }

    public Long getNodeStatusUpdateTime(String service, String node) {
        String path = getStatusPath(service, node);
        return getUpdateTime(path);
    }

    public ServiceNodeStatus getServiceNodeStatus(String service, String node) {
        String statusPath = getStatusPath(service, node);
        ZooKeeperService zkSvc = getZooKeeperService();
        String currStatus = zkSvc.getNodeStringData(statusPath);
        ServiceNodeStatus status = ServiceNodeStatus.INACTIVE;
        if (currStatus != null)
            status = Enum.valueOf(ServiceNodeStatus.class, currStatus);
        return status;
    }

    public EmailCategory getEmailEnabledCategory() {
        String categoryPath = getEmailEnabledCategoryPath();
        ZooKeeperService zkSvc = getZooKeeperService();
        String currCategory = zkSvc.getNodeStringData(categoryPath);
        EmailCategory category = null;
        if (currCategory != null)
            category = Enum.valueOf(EmailCategory.class, currCategory);
        return category;
    }

    public String getUseMasterWatch() {
        String watchPath = getUseMasterWatchPath();
        ZooKeeperService zkSvc = getZooKeeperService();
        String currWatch = zkSvc.getNodeStringData(watchPath);
        String watch = "disabled";
        if (currWatch != null)
            watch = currWatch;
        return watch;
    }

    public String getEmailEnabledCategoryPath() {
        return getPropertiesPath() + "/emailEnabledCategory";
    }

    public String getUseMasterWatchPath() {
        return getPropertiesPath() + "/useMasterWatch";
    }

    public String getPropertiesPath() {
        return getClusterPath() + "/properties";
    }

    public String getPropertiesPath(String service) {
        return getClusterPath()
                + "/" + service + "/properties";
    }

    public String getConfigPath(String service, String node) {
        return getNodePath(service, node) + "/config";
    }

    public String getConfigPath() {
        return getClusterPath() + "/config";
    }

    public String getConfigPath(String service) {
        return getClusterPath()
                + "/" + service + "/config";
    }

    public String getPropertiesPath(String service, String node) {
        return getNodePath(service, node) + "/properties";
    }


    public String getLockPath(String service) {
        return getClusterPath()
                + "/" + service + "/lock";
    }

    public String getLockPath(String service, String node) {
        return getNodePath(service, node) + "/lock";
    }

    public String getHarmonyClusterCheckTimePath() {
        return getClusterPath() + "/harmonyNodes/" + getCurrentNode() + "/lastClusterCheckTime";
    }

    public String getHarmonyNodeInspectionTimePath() {
        return getClusterPath() + "/harmonyNodes/" + getCurrentNode() + "/lastNodeInspectionTime";
    }

    public String getHarmonyWriterVipStatusPath() {
        return getHarmonyWriterVipStatusPath(getCurrentNode());
    }

    private String getHarmonyWriterVipStatusPath(String node) {
        return getClusterPath() + "/harmonyNodes/" + node + "/writerVipStatus";
    }

    public String getHarmonyReaderVipStatusPath() {
        return getHarmonyReaderVipStatusPath(getCurrentNode());
    }

    private String getHarmonyReaderVipStatusPath(String node) {
        return getClusterPath() + "/harmonyNodes/" + node + "/readerVipStatus";
    }

    @Override
    public boolean getNodeReaderVipStatus(String service, String node) {
        String readerVipStatusPath = getHarmonyReaderVipStatusPath(node) + "/" + service;
        ZooKeeperService zkSvc = getZooKeeperService();
        String readerVipStatus = zkSvc.getNodeStringData(readerVipStatusPath);
        return readerVipStatus != null && readerVipStatus.equals("true");
    }

    public List<String> getAllShards() {
        String wholeClusterPath = zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + environment.getClusterType();
        ZooKeeperService zkSvc = getZooKeeperService();
        return zkSvc.getChildren(wholeClusterPath);
    }

    public Cluster getClusterInformation() {
        return getClusterInformation(environment.getCluster());
    }

    public Cluster getClusterInformation(String clusterName) {
        String targetClusterPath = zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + environment.getClusterType()
                + "/" + clusterName;

        try {
            return clusterBuilder.buildCluster(targetClusterPath);
        } catch (InstantiationException e) {
            logger.error("Error build cluster information!", e);
        } catch (IllegalAccessException e) {
            logger.error("Error build cluster information!", e);
        }
        return null;
    }

    public ClusterConfig getClusterConfig() {
        return getClusterConfig(environment.getCluster());
    }

    public ClusterConfig getClusterConfig(String clusterName) {
        String targetClusterPath = zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + environment.getClusterType()
                + "/" + clusterName;

        try {
            return clusterBuilder.buildClusterConfig(targetClusterPath);
        } catch (InstantiationException e) {
            logger.error("Error build cluster information!", e);
        } catch (IllegalAccessException e) {
            logger.error("Error build cluster information!", e);
        }
        return null;
    }

    public HarmonyNodesStatus getClusterHarmonyNodesStatus(String clusterName) {
        String targetClusterPath = zooKeeperConfig.getAppRoot()
                + "/" + environment.getApplication()
                + "/" + environment.getUniverse()
                + "/" + environment.getClusterType()
                + "/" + clusterName;

        try {
            return clusterBuilder.buildHarmonyNodesStatus(targetClusterPath);
        } catch (InstantiationException e) {
            logger.error("Error build cluster information!", e);
        } catch (IllegalAccessException e) {
            logger.error("Error build cluster information!", e);
        }
        return null;
    }

    @Override
    public void setNodeOnline(String serviceName, String nodeName) {
        String nodeStatusPath = getStatusPath(serviceName, nodeName);

        getZooKeeperService().ensurePath(nodeStatusPath);
        getZooKeeperService().setNodeStringData(nodeStatusPath, "ONLINE");
    }

    @Override
    public void setUseMasterWatch(String status) {
        String path = getUseMasterWatchPath();
        getZooKeeperService().ensurePath(path);
        getZooKeeperService().setNodeStringData(path, status);
    }

    @Override
    public ServiceProperties getAutoFailoverConfig(String serviceName) {
        String propertiesPath = getPropertiesPath(serviceName);
        try {
            return clusterBuilder.buildAutoFailoverConfig(propertiesPath);
        } catch (InstantiationException e) {
            logger.error("Error build auto failover configuration!", e);
        } catch (IllegalAccessException e) {
            logger.error("Error build auto failover configuration!", e);
        }
        return null;
    }

    @Override
    public void setAutoFailoverQuota(String serviceName, Long newQuota, boolean isWriter) {
        String propertiesPath = getPropertiesPath(serviceName);
        String failoverQuotaPath = propertiesPath + (isWriter ? "/autoFailoverQuota" : "/autoFailoverQuotaReader");
        getZooKeeperService().ensurePath(failoverQuotaPath);
        getZooKeeperService().setNodeLongData(failoverQuotaPath, newQuota);
    }

    @Override
    public void setAutoFailoverTrigger(String serviceName, Long newVal, boolean isWriter) {
        String propertiesPath = getPropertiesPath(serviceName);
        String failoverTriggerPath = propertiesPath + (isWriter ? "/autoFailoverTrigger" : "/autoFailoverTriggerReader");
        getZooKeeperService().ensurePath(failoverTriggerPath);
        getZooKeeperService().setNodeLongData(failoverTriggerPath, newVal);
    }

    @Override
    public void setMailLevel(String newMailLevel) {
        EmailCategory category = Enum.valueOf(EmailCategory.class, newMailLevel);
        String emailEnabledCategoryPath = getEmailEnabledCategoryPath();
        getZooKeeperService().ensurePath(emailEnabledCategoryPath);
        getZooKeeperService().setNodeStringData(emailEnabledCategoryPath, newMailLevel);
    }

    @Override
    public void setCurrentNodeAsHarmonyLeader() {
        String harmonyLeaderPath = getClusterPath() + "/harmonyLeader";
        getZooKeeperService().ensurePath(harmonyLeaderPath);
        getZooKeeperService().setNodeStringData(harmonyLeaderPath, getCurrentNode());
    }

    @Override
    public void setAutofailoverMode(String serviceName, String status) {
        AutoFailoverMode.valueOf(status);
        String propertiesPath = getPropertiesPath(serviceName);
        String failoverQuotaPath = propertiesPath + "/autoFailoverMode";
        getZooKeeperService().ensurePath(failoverQuotaPath);
        getZooKeeperService().setNodeStringData(failoverQuotaPath, status);
    }
}
