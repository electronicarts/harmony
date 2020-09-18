/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyName;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodeStatus;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodesStatus;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyVipStatus;
import com.ea.eadp.harmony.command.annotation.CommandPath;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.shared.format.Block;
import com.ea.eadp.harmony.shared.format.Board;
import com.ea.eadp.harmony.shared.format.Table;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by VincentZhang on 4/26/2018.
 */
@Component
public class ClusterHealth {
    public static long CHECK_TIME_OUT = 300000l;

    @Autowired
    ClusterManager clusterManager;

    @Autowired
    Gson gson;

    @Autowired
    ServiceConfigRepository serviceConfigRepository;

    @Autowired
    TemplateRenderer templateRenderer;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @CommandPath(path = "/cluster/harmony_status/<clusterName>")
    public String getClusterInfo(String clusterName) {
        HarmonyNodesStatus nodesStatus = clusterManager.getClusterHarmonyNodesStatus(clusterName);
        return gson.toJson(nodesStatus);
    }

    public String healthCheckService(String clusterName) {
        Map<String, Object> dataMapping = new HashMap<>();
        dataMapping.put("cluster", clusterName);

        // Check the overall status of this cluster
        // 1. Prepare variables for harmony health check.
        // Collect all running harmony node name into a map
        Map<String, Boolean> harmonyNodeRunningStatus = new HashMap();
        HarmonyNodesStatus harmonyNodesStatus = clusterManager.getClusterHarmonyNodesStatus(clusterName);
        for (String runningHarmonyNodeNames : harmonyNodesStatus.leader.keySet()) {
            HarmonyName name = harmonyNodesStatus.leader.get(runningHarmonyNodeNames);
            harmonyNodeRunningStatus.put(name.nodeName, true);
        }
        Cluster clusterStatus = clusterManager.getClusterInformation(clusterName);

        dataMapping.put("CHECK_TIME_OUT", CHECK_TIME_OUT);
        dataMapping.put("harmonyNodesStatus", harmonyNodesStatus);
        dataMapping.put("harmonyNodeRunningStatus", harmonyNodeRunningStatus);
        dataMapping.put("clusterStatus", clusterStatus);
        dataMapping.put("clusterConfig", clusterManager.getClusterConfig(clusterName));

        // 2. Prepare variables for VIP status check.
        Map<String, String> nodeHoldingWriterVipForService = new HashMap();
        Map<String, String> nodeHoldingReaderVipForService = new HashMap();

        for (String harmonyNodeName : harmonyNodesStatus.harmonyNodes.keySet()) {
            HarmonyNodeStatus nodeStatus = harmonyNodesStatus.harmonyNodes.get(harmonyNodeName);
            for (String serviceName : nodeStatus.writerVipStatus.keySet()) {
                HarmonyVipStatus vipStatus = nodeStatus.writerVipStatus.get(serviceName);
                checkVipStatus(serviceName, harmonyNodeName, vipStatus, nodeHoldingWriterVipForService);
            }
            for (String serviceName : nodeStatus.readerVipStatus.keySet()) {
                HarmonyVipStatus vipStatus = nodeStatus.readerVipStatus.get(serviceName);
                checkVipStatus(serviceName, harmonyNodeName, vipStatus, nodeHoldingReaderVipForService);
            }
        }

        dataMapping.put("nodeHoldingWriterVipForService", nodeHoldingWriterVipForService);
        dataMapping.put("nodeHoldingReaderVipForService", nodeHoldingReaderVipForService);

        String returnMessage = templateRenderer.renderTemplate(this.getClass(), "cluster_command_output", dataMapping);
        // 3. Check service status
        returnMessage += clusterStatus.checkServiceStatus(templateRenderer);

        return returnMessage;

    }

    private void checkVipStatus(String serviceName, String harmonyNodeName, HarmonyVipStatus vipStatus, Map<String, String> nodeHoldingVipForService) {
        if (vipStatus.currentHoldingVip.equalsIgnoreCase("true")) {
            String currentVipNode = nodeHoldingVipForService.get(serviceName);
            if (null == currentVipNode) {
                nodeHoldingVipForService.put(serviceName, harmonyNodeName);
            } else {
                nodeHoldingVipForService.put(serviceName, currentVipNode + "," + harmonyNodeName);
            }
        } else {
            if (nodeHoldingVipForService.get(serviceName) == null) {
                // Place holder to ensure be checked later.
                nodeHoldingVipForService.put(serviceName, null);
            }
        }
    }

    @CommandPath(path = "/cluster/health_check/<clusterName>")
    public String healthCheck(String clusterName) {
        return healthCheckService(clusterName);
    }

    @CommandPath(path = "/cluster/harmony_health_check/<clusterName>")
    public String healthCheckHarmonyInTable(String clusterName) {
        Map<String, Boolean> harmonyNodeRunningStatus = new HashMap();
        HarmonyNodesStatus harmonyNodesStatus = clusterManager.getClusterHarmonyNodesStatus(clusterName);
        for (String runningHarmonyNodeNames : harmonyNodesStatus.leader.keySet()) {
            HarmonyName name = harmonyNodesStatus.leader.get(runningHarmonyNodeNames);
            harmonyNodeRunningStatus.put(name.nodeName, true);
        }

        List<String> headersList = Arrays.asList("NODE NAME", "RUNNING", "ROLE", "UPDATE TIME");
        List<List<String>> rowsList = new ArrayList<>();
        for (String harmonyNodeName : harmonyNodesStatus.harmonyNodes.keySet()) {
            HarmonyNodeStatus nodeStatus = harmonyNodesStatus.harmonyNodes.get(harmonyNodeName);
            rowsList.add(Arrays.asList(
                    harmonyNodeName,
                    harmonyNodeRunningStatus.containsKey(harmonyNodeName) ? "Yes" : "No",
                    harmonyNodeName.equals(harmonyNodesStatus.harmonyLeader) ? "leader" : "follower",
                    simpleDateFormat.format(new Date(Long.parseLong(nodeStatus.getLastNodeInspectionTime())))
            ));
        }


        Board board = new Board(75);

        Table table = new Table(board, 75, headersList, rowsList);
        List<Integer> colWidthsListEdited = Arrays.asList(15, 10, 10, 20);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);

        Block block = table.tableToBlocks();
        board.setInitialBlock(block);
        return board.build().getPreview();
    }

    @CommandPath(path = "/cluster/service_health_check/<clusterName>")
    public String healthCheckServiceInTable(String clusterName) {
        Cluster clusterStatus = clusterManager.getClusterInformation(clusterName);
        return clusterStatus.checkServiceStatusInTable();
    }
}
