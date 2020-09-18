/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.cluster;

import com.ea.eadp.harmony.cluster.ClusterManagerImpl;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.stereotype.Component;

/**
 * Created by juding on 5/18/16.
 */
@Component
public class RedisClusterManagerImpl extends ClusterManagerImpl {
    @Override
    protected void enhanceServicePath(String service, String path, ZooKeeperService zkSvc) {
        RedisServiceConfig config = (RedisServiceConfig) serviceConfigRepository.getServiceConfig(service);
        super.enhanceServicePath(path, zkSvc, config);
    }
}
