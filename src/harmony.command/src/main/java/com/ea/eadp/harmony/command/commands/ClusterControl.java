/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ClusterConfig;
import com.ea.eadp.harmony.cluster.entity.NodeConfig;
import com.ea.eadp.harmony.cluster.entity.ServiceConfig;
import com.ea.eadp.harmony.command.annotation.CommandPath;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.transition.TransitionConductor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;

/**
 * Created by VincentZhang on 5/24/2018.
 */
@Component
public class ClusterControl {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private HessianFactory hessianFactory;

    @Autowired
    private HarmonyEnvironment environment;

    private String internalMasterMoveTo(String clusterName, String serviceName, String newMasterNodeName, boolean forceMove) {
        Pair<TransitionConductor, String> conductorWithMsg = getConductor(clusterName, serviceName, newMasterNodeName);
        TransitionConductor conductor = conductorWithMsg.getLeft();
        if (conductor != null) {
            if (!forceMove)
                conductor.moveMaster(serviceName, newMasterNodeName);
            else
                conductor.forceMoveMaster(serviceName, newMasterNodeName);

            return conductorWithMsg.getLeft() + "OK"; 
        } else {
            return conductorWithMsg.getLeft() + "Conductor is null, can't failover!";
        }
    }

    private String internalFixReplication(String clusterName, String serviceName) {
        String retValue = "OK";
        try {
            environment.setCluster(clusterName);
            String currentMaster = clusterManager.getCurrentMaster(serviceName);
            String currentSlave = clusterManager.getCurrentPrimarySlave(serviceName);
            if (StringUtils.isBlank(currentMaster) || StringUtils.isBlank(currentSlave)) {
                throw new Exception("currentMaster or currentSlave is not set. please check the service status.");
            } else {
                Pair<TransitionConductor, String> conductorWithMsg = getConductor(clusterName, serviceName, null);
                if (conductorWithMsg.getLeft() != null) {
                    conductorWithMsg.getLeft().onlineSlave(serviceName, currentSlave, null);
                } else {
                    return "ERROR\n" + conductorWithMsg.getRight();
                }
            }
        } catch (Exception e) {
            retValue = "ERROR\n" + e;
        }
        return retValue;
    }

    private String internalReaderMoveTo(String clusterName, String serviceName, String newReaderNodeName) {
        Pair<TransitionConductor, String> conductorWithMsg = getConductor(clusterName, serviceName, newReaderNodeName);
        TransitionConductor conductor = conductorWithMsg.getLeft();
        if (conductor != null) {
            conductor.moveReader(serviceName, newReaderNodeName);
            return conductorWithMsg.getRight() + "OK"; 
        } else {
            return conductorWithMsg.getRight() + "Conductor is null, can't failover!";
        }
    }

    private Pair<TransitionConductor, String> getConductor(String clusterName, String serviceName, String newNodeName) {
        Cluster clusterStatus = null;
        try {
            clusterStatus = clusterManager.getClusterInformation(clusterName);
        } catch (RuntimeException e) {
            throw new RuntimeException("Cluster:" + clusterName + "not found! Run command: \"clusters name\"" + " and find valid cluster names.");
        }

        ClusterConfig clusterConfig = clusterManager.getClusterConfig(clusterName);

        String harmonyMasterNodeName = clusterStatus.harmonyLeader;
        ServiceConfig nodeConfigs = clusterConfig.serviceNodes.get(serviceName);
        if (nodeConfigs == null) {
            return new ImmutablePair<>(null, "Service:" + serviceName + " not found. Possible services are:" + clusterConfig.serviceNodes.keySet());
        } else if (newNodeName != null && nodeConfigs.nodes.get(newNodeName) == null) {
            return new ImmutablePair<>(null, "Node:" + newNodeName + " not found. Possible nodes for service:"
                    + serviceName + " are:" + nodeConfigs.nodes.keySet());
        }

        NodeConfig masterNodeConfig = nodeConfigs.nodes.get(harmonyMasterNodeName);
        String retValue = "Sending node transition Hessian call to:" + masterNodeConfig.hostName + ":" + masterNodeConfig.harmonyServerPort + "\n";
        TransitionConductor conductor = null;
        try {
            conductor = hessianFactory.transitionConductor(masterNodeConfig.hostName, masterNodeConfig.harmonyServerPort);
        } catch (MalformedURLException e) {
            return new ImmutablePair<>(null, "URL is wrong, exception happened." + e.getMessage());
        }

        return new ImmutablePair<>(conductor, retValue);
    }

    @CommandPath(path = "/cluster/master_move_to/<clusterName>/<serviceName>/<newMasterNodeName>")
    public String masterMoveTo(String clusterName, String serviceName, String newMasterNodeName) {
        return internalMasterMoveTo(clusterName, serviceName, newMasterNodeName, false);
    }

    @CommandPath(path = "/cluster/force_master_move_to/<clusterName>/<serviceName>/<newMasterNodeName>")
    public String forceMasterMoveTo(String clusterName, String serviceName, String newMasterNodeName) {
        return internalMasterMoveTo(clusterName, serviceName, newMasterNodeName, true);
    }

    @CommandPath(path = "/cluster/fix_replication/<clusterName>/<serviceName>")
    public String fixReplication(String clusterName, String serviceName) {
        return internalFixReplication(clusterName, serviceName);
    }

    @CommandPath(path = "/cluster/reader_move_to/<clusterName>/<serviceName>/<newReaderNodeName>")
    public String readerMoveTo(String clusterName, String serviceName, String newReaderNodeName) {
        return internalReaderMoveTo(clusterName, serviceName, newReaderNodeName);
    }
}
