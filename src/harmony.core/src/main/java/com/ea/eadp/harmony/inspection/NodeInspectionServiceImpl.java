/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.inspection;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.event.NodeInspectionEvent;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;
import com.ea.eadp.harmony.shared.utils.HarmonyRunnable;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by leilin on 10/16/2014.
 */
@Component
public class NodeInspectionServiceImpl extends ServiceSupport implements NodeInspectionService {
    private final static Logger logger = LoggerFactory.getLogger(NodeInspectionServiceImpl.class);

    @Autowired
    private NodeInspector nodeInspector;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ClusterManager clusterManager;

    final private ExecutorService exeSvc = Executors.newCachedThreadPool();

    @Override
    public void onNodeInspection(NodeInspectionEvent event) {
        // for each inspection target
        for (final ServiceConfig targetConfig : serviceConfigRepository.getInspectionTargets()) {
            exeSvc.execute(new HarmonyRunnable(HarmonyRunnable.getLogContext()) {
                @Override
                public void runInternal() {
                    try {
                        inspectNode(targetConfig);
                    } catch(Exception e) {
                        logger.error("fail to inspect node.", e);
                    }
                }
            });
        }

        String nodeInspectionTimePath = clusterManager.getHarmonyNodeInspectionTimePath();
        updateAndCheckTime(nodeInspectionTimePath);
    }

    @Override
    public void onApplicationClosed(ApplicationClosedEvent e) {
        if (exeSvc != null && !exeSvc.isTerminated()) {
            logger.info("Shutting down node inspection thread pool!");
            exeSvc.shutdown();
        }
    }

    private void inspectNode(ServiceConfig targetConfig) {
        String service = targetConfig.getService();
        String node = targetConfig.getNode();
        // read status
        ServiceNodeStatus status = clusterManager.getServiceNodeStatus(service, node);

        switch (status) {
            case ONLINE:
                // fall through
            case DOWN:
                // inspect
                InspectionResult inspectionResult = nodeInspector.inspectNode(targetConfig);
                if (inspectionResult == null)
                    break;
                // write observation
                String observationPath = clusterManager.getObservationPath(
                        service, node, clusterManager.getCurrentNode());
                ZooKeeperService zkSvc = getZooKeeperService();
                zkSvc.ensurePath(observationPath);
                zkSvc.setNodeStringData(observationPath, inspectionResult.getStatus().name());
                break;
            default:
                logger.info("Skipped inspect /{}/{} status {}",
                        new Object[]{service, node, status});
        }
    }
}
