/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.ea.eadp.harmony.cluster.ClusterManagerImpl;
import com.ea.eadp.harmony.command.annotation.CommandPath;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by VincentZhang on 4/26/2018.
 */
@Component
public class Clusters {
    private static final Logger logger = LoggerFactory.getLogger(Clusters.class);
    @Autowired
    ClusterManagerImpl clusterManager;

    @Autowired
    ClusterHealth clusterHealth;

    @Autowired
    ClusterInfo clusterInfo;

    @Autowired
    Gson gSon;

    @Autowired
    protected ServiceConfigRepository serviceConfigRepository;

    @CommandPath(path = "/clusters/names")
    public String getClusterInfo() {
        return gSon.toJson(clusterManager.getAllShards());
    }

    @CommandPath(path = "/clusters/health_check")
    public String getClustersHealth() {
        String retString = "";
        // Sor the shard names
        List<String> clusterNames = clusterManager.getAllShards();
        Collections.sort(clusterNames);

        for (String clusterName : clusterNames) {
            retString += "Checking:" + clusterName + "\n";
            try {
                retString += clusterHealth.healthCheckService(clusterName);
            } catch (Exception e) {
                retString += "Error happened while trying to process:" + clusterName;
                logger.error("Error happened while trying to process:" + clusterName, e);
            }
            retString += "\n";
        }
        return retString;
    }

    @CommandPath(path = "/clusters/set_mail_level/<newEmailLevel>")
    public String setMailLevel(String newEmailLevel) {
        try{
            EmailCategory.valueOf(newEmailLevel);
        }catch (IllegalArgumentException e){
            return "IllegalArgumentException. NewEmailLevel can only be: " + EmailCategory.getAllValues();
        }

        List<String> clusterNames = clusterManager.getAllShards();
        for (String clusterName : clusterNames) {
            clusterInfo.setClusterMailLevel(clusterName, newEmailLevel);
        }

        return "OK";
    }

    @CommandPath(path = "/clusters/harmony_health_check")
    public String getClustersHarmonyHealth() {
        String retString = "";
        List<String> clusterNames = clusterManager.getAllShards();
        Collections.sort(clusterNames);

        for (String clusterName : clusterNames) {
            retString += "Checking:" + clusterName + "\n";
            try {
                retString += clusterHealth.healthCheckHarmonyInTable(clusterName);
            } catch (Exception e) {
                retString += "Error happened while trying to process:" + clusterName;
                logger.error("Error happened while trying to process:" + clusterName, e);
            }
            retString += "\n";
        }
        return retString;
    }

    @CommandPath(path = "/clusters/service_health_check")
    public String getClustersServiceHealth() {
        String retString = "";
        List<String> clusterNames = clusterManager.getAllShards();
        Collections.sort(clusterNames);

        for (String clusterName : clusterNames) {
            retString += "Checking:" + clusterName + "\n";
            try {
                retString += clusterHealth.healthCheckServiceInTable(clusterName);
            } catch (Exception e) {
                retString += "Error happened while trying to process:" + clusterName;
                logger.error("Error happened while trying to process:" + clusterName, e);
            }
            retString += "\n";
        }
        return retString;
    }
}
