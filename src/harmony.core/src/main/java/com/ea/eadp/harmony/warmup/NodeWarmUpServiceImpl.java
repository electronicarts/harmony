/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.warmup;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.event.NodeWarmUpEvent;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;
import com.ea.eadp.harmony.shared.event.ApplicationStartedEvent;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class NodeWarmUpServiceImpl extends ServiceSupport implements NodeWarmUpService {
    private final static Logger logger = LoggerFactory.getLogger(NodeWarmUpServiceImpl.class);

    private final static String SWITCH_ENABLE = "enable";

    @Autowired
    private NodeWarmUpExecutor nodeWarmUpExecutor;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ClusterManager clusterManager;

    private NodeCache warmUpSwitchCache;

    private boolean warmUpRunning;

    @Override
    public void onApplicationStarted(ApplicationStartedEvent e) {
        String warmUpSwitchPath = clusterManager.getWarmUpSwitchPath();
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.ensurePath(warmUpSwitchPath);
        warmUpSwitchCache = zkSvc.createNodeCache(warmUpSwitchPath, new MonitorWarmUpSwitchListener());
        warmUpRunning = false;
    }

    @Override
    public void onNodeWarmUp(NodeWarmUpEvent event) {
        if (warmUpRunning) {
            return;
        }
        for (final ServiceConfig targetConfig : serviceConfigRepository.getInspectionTargets()) {
            if (targetConfig.getNode().equals(serviceConfigRepository.getCurrentNode())) {
                String curSlave = clusterManager.getCurrentPrimarySlave(targetConfig.getService());
                if (clusterManager.getCurrentNode().equals(curSlave)) {
                    String switchState = getZooKeeperService().getNodeStringData(clusterManager.getWarmUpSwitchPath());
                    if (switchState != null && switchState.equalsIgnoreCase(SWITCH_ENABLE)) {
                        logger.info("Current node is a slave. Start to run warm-up queries.");
                        warmUpRunning = true;
                        nodeWarmUpExecutor.warmUpNode(targetConfig);
                        logger.info("Warm up ended");
                        warmUpRunning = false;
                    } else {
                        logger.info("Warm up is disabled");
                    }
                } else {
                    logger.info("Current node is not a slave. No need to do warm-up");
                }
            }
        }
    }

    private class MonitorWarmUpSwitchListener implements NodeCacheListener {
        @Override
        public void nodeChanged() {
            String switchState = getZooKeeperService().getNodeStringData(clusterManager.getWarmUpSwitchPath());
            if (switchState == null || !switchState.equalsIgnoreCase(SWITCH_ENABLE)) {
                for (final ServiceConfig targetConfig : serviceConfigRepository.getInspectionTargets()) {
                    if (targetConfig.getNode().equals(serviceConfigRepository.getCurrentNode())) {
                        logger.info("Warm up is disabled. Kill running queries");
                        String curSlave = clusterManager.getCurrentPrimarySlave(targetConfig.getService());
                        if (clusterManager.getCurrentNode().equals(curSlave)) {
                            nodeWarmUpExecutor.killWarmUp(targetConfig);
                        }
                    }
                }
            } else {
                logger.info("Warm up is enabled");
            }
        }
    }

    @Override
    public void onApplicationClosed(ApplicationClosedEvent e) {
        try {
            if (warmUpSwitchCache != null) {
                warmUpSwitchCache.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
