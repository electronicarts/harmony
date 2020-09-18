/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster;

import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ClusterConfig;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodesStatus;
import com.ea.eadp.harmony.cluster.entity.ServiceProperties;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.shared.email.EmailCategory;

import java.util.List;

/**
 * User: leilin
 * Date: 10/2/14
 */
public interface ClusterManager {
    String getCurrentCluster();

    String getCurrentNode();

    String getWarmUpSwitchPath();

    String getClusterPath();

    String getLeaderPath();

    String getRunnerCountPath();

    String getServicePath(String service);

    String getNodePath(String service, String node);

    String getObservationPath(String service, String subjectNode, String observerNode);

    String getObservationsPath(String service, String node);

    String getStatusPath(String service, String node);

    String getMarkerPath(String service, String node);

    String getMarkerStepPath(String service, String node);

    String getMasterPath(String service);

    String getPrimarySlavePath(String service);

    String getCurrentMaster(String service);

    String getCurrentPrimarySlave(String service);

    void setCurrentMaster(String service, String value);

    void setCurrentPrimarySlave(String service, String value);

    void initClusterInZooKeeper();

    Long getZooKeeperTime(String service);

    Long getUpdateZxid(String path);

    Long getUpdateTime(String path);

    Long getMasterZxid(String service);

    Long getNodeStatusUpdateZxid(String service, String node);

    Long getNodeStatusUpdateTime(String service, String node);

    ServiceNodeStatus getServiceNodeStatus(String service, String node);

    EmailCategory getEmailEnabledCategory();

    String getUseMasterWatch();

    String getEmailEnabledCategoryPath();

    String getUseMasterWatchPath();

    String getPropertiesPath();

    String getPropertiesPath(String service);

    String getPropertiesPath(String service, String node);

    String getConfigPath();

    String getConfigPath(String service);

    String getConfigPath(String service, String node);

    String getLockPath(String service);

    String getLockPath(String service, String node);

    String getHarmonyClusterCheckTimePath();

    String getHarmonyNodeInspectionTimePath();

    String getHarmonyWriterVipStatusPath();

    String getHarmonyReaderVipStatusPath();

    boolean getNodeReaderVipStatus(String service, String node);

    List<String> getAllShards();

    Cluster getClusterInformation();

    Cluster getClusterInformation(String clusterName);

    ClusterConfig getClusterConfig(String clusterName);

    ClusterConfig getClusterConfig();

    HarmonyNodesStatus getClusterHarmonyNodesStatus(String clusterName);

    void setNodeOnline(String serviceName, String nodeName);

    void setUseMasterWatch(String status);

    ServiceProperties getAutoFailoverConfig(String serviceName);

    void setAutoFailoverQuota(String serviceName, Long newQuota, boolean isWriter);

    void setAutoFailoverTrigger(String serviceName, Long newVal, boolean isWriter);

    void setMailLevel(String newMailLevel);

    void setCurrentNodeAsHarmonyLeader();

    void setAutofailoverMode(String serviceName, String status);
}
