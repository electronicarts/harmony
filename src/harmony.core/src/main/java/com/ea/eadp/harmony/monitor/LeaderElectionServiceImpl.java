/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.event.EmailCategoryChangedEvent;
import com.ea.eadp.harmony.event.ServiceMasterChangedEvent;
import com.ea.eadp.harmony.shared.event.ApplicationStartedEvent;
import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;
import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.event.MonitorLeaderChangedEvent;
import com.ea.eadp.harmony.service.ServiceSupport;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: leilin
 * Date: 10/8/14
 */
@Component
public class LeaderElectionServiceImpl extends ServiceSupport implements LeaderElectionService {
    private final static Logger logger = LoggerFactory.getLogger(LeaderElectionServiceImpl.class);

    @Autowired
    private ClusterManager clusterManager;

    private LeaderLatch leaderLatch;

    private Map<String, NodeCache> nodeCacheMap = new HashMap<String, NodeCache>();

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Override
    public void onApplicationStarted(ApplicationStartedEvent e) {
        logger.info("Application started, electing monitor lead");

        String leaderElectionPath = clusterManager.getLeaderPath();
        String currCluster = clusterManager.getCurrentCluster();
        String currNode = clusterManager.getCurrentNode();

        // init leader latch
        leaderLatch = getZooKeeperService().createLeaderLatch(currNode, leaderElectionPath, new MonitorLeaderListener(currCluster));

        clusterManager.initClusterInZooKeeper();

        String categoryPath = clusterManager.getEmailEnabledCategoryPath();
        NodeCache categoryNodeCache = getZooKeeperService().createNodeCache(categoryPath, new CategoryNodeCacheListener());
        nodeCacheMap.put(categoryPath, categoryNodeCache);

        for (String service : serviceConfigRepository.getServiceList()) {
            String masterPath = clusterManager.getMasterPath(service);
            NodeCache masterNodeCache = getZooKeeperService().createNodeCache(masterPath, new MonitorNodeCacheListener(service, currNode));
            nodeCacheMap.put(masterPath, masterNodeCache);

            // The above createNodeCache() will trigger this.
            // transitionConductor.ensureMaster(service, currNode);
        }
    }

    private class MonitorLeaderListener implements LeaderLatchListener {
        private String clusterId;

        private MonitorLeaderListener(String clusterId) {
            this.clusterId = clusterId;
        }

        @Override
        public void isLeader() {
            logger.info("become leader for " + clusterId);
            rasieEvent(new MonitorLeaderChangedEvent(clusterId, true));

            // Write this information into ZKPR
            clusterManager.setCurrentNodeAsHarmonyLeader();
            // postEmail(clusterManager.getCurrentNode() + " LEADER", clusterManager.getLeaderPath());
        }

        @Override
        public void notLeader() {
            logger.info("not leader for " + clusterId);
            rasieEvent(new MonitorLeaderChangedEvent(clusterId, false));
        }
    }

    private class MonitorNodeCacheListener implements NodeCacheListener {
        private String service;
        private String node;

        private MonitorNodeCacheListener(String service, String node) {
            this.service = service;
            this.node = node;
        }

        @Override
        public void nodeChanged() throws Exception {
            logger.info("master changed for " + service + "/" + node);
            rasieEventAsync(new ServiceMasterChangedEvent(service, node));
        }
    }

    private class CategoryNodeCacheListener implements NodeCacheListener {
        private CategoryNodeCacheListener() {
        }

        @Override
        public void nodeChanged() throws Exception {
            logger.info("email enabled category changed");
            rasieEvent(new EmailCategoryChangedEvent());
        }
    }

    @Override
    public void onApplicationClosed(ApplicationClosedEvent e) {
        logger.info("Application closed, closing monitor lead/node caches and email service.");

        try {
            if (leaderLatch != null)
                leaderLatch.close();
            for (NodeCache nodeCache : nodeCacheMap.values())
                nodeCache.close();

            onApplicatoinClosed();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
