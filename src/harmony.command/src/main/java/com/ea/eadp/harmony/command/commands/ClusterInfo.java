/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.ClusterConfig;
import com.ea.eadp.harmony.command.annotation.CommandPath;
import com.ea.eadp.harmony.config.AutoFailoverMode;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 4/26/2018.
 */
@Component
public class ClusterInfo {
    @Autowired
    ClusterManager clusterManager;

    @Autowired
    Gson gson;

    @Autowired
    private HarmonyEnvironment environment;

    private String errorMsg = "Command format error. Should be ";

    @CommandPath(path = "/cluster/get_status/<clusterName>")
    public String getClusterHealth(String clusterName) {
        return gson.toJson(clusterManager.getClusterInformation(clusterName));
    }

    @CommandPath(path = "/cluster/get_config/<clusterName>")
    public String getClusterConfig(String clusterName) {
        return gson.toJson(clusterManager.getClusterConfig(clusterName));
    }

    @CommandPath(path = "/cluster/set_online/<clusterName>/<serviceName>/<nodeName>")
    public String setServiceNodeOnline(String clusterName, String serviceName, String nodeName) {
        environment.setCluster(clusterName);
        clusterManager.setNodeOnline(serviceName, nodeName);
        String currentMaster = clusterManager.getCurrentMaster(serviceName);
        if (currentMaster == null) {
            clusterManager.setCurrentMaster(serviceName, nodeName);
        } else {
            if (!currentMaster.equals(nodeName)) {
                String currentSlave = clusterManager.getCurrentPrimarySlave(serviceName);
                if (currentSlave == null) {
                    clusterManager.setCurrentPrimarySlave(serviceName, nodeName);
                }
            }
        }

        // Use master watch for the cluster
        environment.setCluster(clusterName);
        clusterManager.setUseMasterWatch("enabled");
        return gson.toJson(clusterManager.getClusterInformation(clusterName));
    }

    @CommandPath(path = "/cluster/set_autofailover/<clusterName>/<service>/<status>")
    public String setAutoFailoverMode(String clusterName, String service, String status) {
        try {
            AutoFailoverMode.valueOf(status);
        } catch (IllegalArgumentException e) {
            return errorMsg + " \"cluster set_autofailover <ClusterName> shadow|enabled|disabled\"";
        }

        environment.setCluster(clusterName);
        clusterManager.setAutofailoverMode(service, status);
        return "OK";
    }

    @CommandPath(path = "/cluster/get_autofailover_config/<clusterName>/<serviceName>")
    public String getAutoFailoverConfig(String clusterName, String serviceName) {
        ClusterConfig clusterConfig = clusterManager.getClusterConfig(clusterName);
        return gson.toJson(clusterConfig.serviceNodes.get(serviceName).properties);
    }

    @CommandPath(path = "/cluster/set_autofailover_quota/<clusterName>/<serviceName>/<newQuota>")
    public String setAutoFailoverQuota(String clusterName, String serviceName, String newQuota) {
        environment.setCluster(clusterName);
        Long newQuotaL = Long.valueOf(newQuota);

        clusterManager.setAutoFailoverQuota(serviceName, newQuotaL, true);
        clusterManager.setAutoFailoverQuota(serviceName, newQuotaL, false);
        return "OK";
    }

    @CommandPath(path = "/cluster/get_mail_level/<clusterName>")
    public String getClusterMailLevel(String clusterName) {
        environment.setCluster(clusterName);
        EmailCategory category = clusterManager.getEmailEnabledCategory();
        return category != null ? category.toString() : "Not set. Using default value: NEVER";
    }

    @CommandPath(path = "/cluster/set_mail_level/<clusterName>/<newMailLevel>")
    public String setClusterMailLevel(String clusterName, String newMailLevel) {
        environment.setCluster(clusterName);
        try {
            clusterManager.setMailLevel(newMailLevel);
        } catch (IllegalArgumentException e) {
            return "IllegalArgumentException. NewEmailLevel can only be: " + EmailCategory.getAllValues();
        }

        return "OK";
    }
}
