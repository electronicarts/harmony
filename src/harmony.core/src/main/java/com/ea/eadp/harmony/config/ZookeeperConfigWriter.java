/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.shared.event.*;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by VincentZhang on 5/2/2018.
 */
@EventHandlerClass
@Component
public class ZookeeperConfigWriter {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperConfigWriter.class);

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ClusterManager clusterManager;

    // zk support
    @Autowired
    private ZooKeeperService zooKeeperService;

    @EventHandler(AfterApplicationStartedEvent.class)
    public void writeConfigIntoZookeeper() {
        List<String> services = serviceConfigRepository.getServiceList();
        for (String service : services) {
            VipServiceConfig config = (VipServiceConfig) serviceConfigRepository.getServiceConfig(service);
            logger.info("Writting config of current node into ZooKeeper /{}/{} {}:{}, harmony port: {}",
                    new Object[]{config.getService(), config.getNode(), config.getHost(), config.getPort(), config.getHarmonyServerPort()});

            String configPath = clusterManager.getConfigPath(service, config.getNode());

            zooKeeperService.ensurePath(configPath + "/hostname");
            zooKeeperService.setNodeStringData(configPath + "/hostname", config.getHost());
            zooKeeperService.ensurePath(configPath + "/port");
            zooKeeperService.setNodeLongData(configPath + "/port", (long) config.getPort());
            zooKeeperService.ensurePath(configPath + "/writerVip");
            zooKeeperService.setNodeStringData(configPath + "/writerVip", config.getWriterVip());
            zooKeeperService.ensurePath(configPath + "/harmonyServerPort");
            zooKeeperService.setNodeLongData(configPath + "/harmonyServerPort", (long) config.getHarmonyServerPort());
        }
    }

    @EventHandler(AfterApplicationClosedEvent.class)
    public void onApplicationClosed(AfterApplicationClosedEvent e) {
        zooKeeperService.close();
    }
}
