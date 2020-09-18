/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.warmup;

import com.ea.eadp.harmony.cluster.entity.ServiceConfig;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.warmup.NodeWarmUpExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisWarmUpExecutor extends ServiceConfig implements NodeWarmUpExecutor<RedisServiceConfig> {
    private final static Logger logger = LoggerFactory.getLogger(RedisWarmUpExecutor.class);

    @Override
    public void warmUpNode(RedisServiceConfig config) {
        // do nothing
    }

    @Override
    public void killWarmUp(RedisServiceConfig config){
        // do nothing
    }
}
