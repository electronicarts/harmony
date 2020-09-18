/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor.ClusterCheckSteps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStep;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyName;
import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodesStatus;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class HarmonyRunningCountCheck extends NodeCheckStep {
    private final static Logger logger = LoggerFactory.getLogger(HarmonyRunningCountCheck.class);

    public String rootCause() {
        return "Harmony running count is wrong!";
    }

    public String action() {
        return "Check if all harmony processes are running on all the nodes marked as DOWN.";
    }

    @Override
    protected String getTemplateName() {
        return "HarmonyRunningCount";
    }

    private Map dataObjectMap;

    @Override
    public NodeCheckStepResult check(Map dataObjectMap) {
        this.dataObjectMap = dataObjectMap;
        String clusterName = (String) NodeCheckContext.get("cluster");
        logger.info("checking health for " + clusterName);
        dataObjectMap.put("cluster", clusterName);

        ZooKeeperService zkSvc = getZooKeeperService();
        String rcPath = getClusterManager().getRunnerCountPath();
        String ldPath = getClusterManager().getLeaderPath();
        long oldCount = zkSvc.getNodeLongData(rcPath);
        List<String> runnerList = zkSvc.getChildren(ldPath);
        long newCount = runnerList.size();
        if (newCount != oldCount) {
            dataObjectMap.put("old_count", oldCount);
            dataObjectMap.put("new_count", newCount);

            zkSvc.setNodeLongData(rcPath, newCount);

            // Send email
            Set<String> upNodes = new HashSet<>();
            HarmonyNodesStatus nodesStatus = getClusterManager().getClusterHarmonyNodesStatus(clusterName);
            for (HarmonyName nodeName : nodesStatus.leader.values()) {
                upNodes.add(nodeName.nodeName);
            }

            dataObjectMap.put("up_nodes", upNodes);

            Map<String, String> downNodes = new HashMap();
            for (ServiceConfig targetConfig : getServiceConfigRepository().getInspectionTargets()) {
                String service = targetConfig.getService();
                String node = targetConfig.getNode();
                if (!upNodes.contains(node)) {
                    String hostName = getHostName(service, node);
                    downNodes.put(node, hostName);
                }
            }

            dataObjectMap.put("down_nodes", downNodes);
            return NodeCheckStepResult.ERROR;
        }

        return NodeCheckStepResult.SUCCEEDED;
    }
}
