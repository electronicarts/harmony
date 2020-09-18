/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.control.ServiceNodeMarker;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.inspection.InspectionResult;
import com.ea.eadp.harmony.inspection.NodeInspector;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.inspection.steps.MySQLInspectStepsChain;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by leilin on 10/16/2014.
 */
@Component
public class MySqlInspector extends ServiceSupport implements NodeInspector<MySqlServiceConfig> {
    private final static Logger logger = LoggerFactory.getLogger(MySqlInspector.class);

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private MySQLInspectStepsChain inspectStepsChain;

    @Override
    public synchronized InspectionResult inspectNode(MySqlServiceConfig config) {
        logger.info("Inspecting MySQL service /{}/{} {}:{}",
                new Object[]{config.getService(), config.getNode(), config.getHost(), config.getPort()});

        String service = config.getService();
        NodeCheckContext.put("config", config);
        NodeCheckContext.put("master", clusterManager.getCurrentMaster(service));
        NodeCheckContext.put("cluster", clusterManager.getCurrentCluster());

        InspectionResult inspectionResult = new InspectionResult(ServiceNodeStatus.ONLINE);
        if (inspectStepsChain.handle().equals(NodeCheckStepResult.ERROR)) {
            inspectionResult = new InspectionResult(ServiceNodeStatus.DOWN);
        }

        logger.info("Got " + inspectionResult);
        return inspectionResult;
    }

    private void setServiceNodeMarker(String service, String node, ServiceNodeMarker marker) {
        String atPath = clusterManager.getMarkerPath(service, node);
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.setNodeStringData(atPath, marker.name());
    }
}
