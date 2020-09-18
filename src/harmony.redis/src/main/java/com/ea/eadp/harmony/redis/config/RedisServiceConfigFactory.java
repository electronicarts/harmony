/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.config;

import com.ea.eadp.harmony.config.BaseServiceConfigFactory;
import org.springframework.stereotype.Component;

/**
 * Created by leilin on 10/16/2014.0
 */
@Component
public class RedisServiceConfigFactory extends BaseServiceConfigFactory<RedisServiceConfig> {
    public RedisServiceConfigFactory() {
        super(RedisServiceConfig.class);
    }
}
