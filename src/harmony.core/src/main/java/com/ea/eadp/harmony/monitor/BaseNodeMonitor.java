/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by juding on 10/23/2014.
 */
@Component
public class BaseNodeMonitor extends ServiceSupport implements NodeMonitor<BaseServiceConfig>,InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(BaseNodeMonitor.class);

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Value("${monitor.nodeInspection.fresh}")
    private long observationFresh;

    @Override
    public MonitorResult monitorNode(BaseServiceConfig config) {
        logger.info("Monitoring service /{}/{} {}:{}",
                new Object[]{config.getService(), config.getNode(), config.getHost(), config.getPort()});

        ZooKeeperService zkSvc = getZooKeeperService();
        String path = clusterManager.getObservationsPath(config.getService(), config.getNode());
        List<String> children = zkSvc.getChildren(path);
        long fullCount = serviceConfigRepository.getInspectorCount();

        if (children.isEmpty())
            return null;

        long currTime = clusterManager.getZooKeeperTime(config.getService());

        Map<String, Integer> observationCounter = new HashMap<String, Integer>();
        for (String child : children) {
            String childPath = path + "/" + child;
            long childUpdateTime = clusterManager.getUpdateTime(childPath);

            if (currTime - childUpdateTime >= observationFresh) {
                logger.warn("observation " + childPath + " not fresh " + observationFresh + ": " + childUpdateTime + " " + currTime);
                continue;
            }

            String status = zkSvc.getNodeStringData(childPath);
            Integer count = observationCounter.get(status);
            if (count == null)
                count = 0;
            observationCounter.put(status, count + 1);
        }

        int maxCount = 0;
        String maxString = null;
        for (Map.Entry<String, Integer> entry : observationCounter.entrySet()) {
            int currCount = entry.getValue();
            if (currCount > maxCount) {
                maxCount = currCount;
                maxString = entry.getKey();
            }
        }

        if (maxCount * 2 <= fullCount)
            return null;

        ServiceNodeStatus maxStatus = Enum.valueOf(ServiceNodeStatus.class, maxString);
        MonitorResult monitorResult = new MonitorResult(maxStatus);
        logger.info("Got " + monitorResult);

        return monitorResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
