/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.config;

import com.ea.eadp.harmony.config.AutoFailoverServiceConfig;
import com.ea.eadp.harmony.config.annotation.ServiceProperty;

/**
 * Created by leilin on 10/15/2014.
 */
public class RedisServiceConfig extends AutoFailoverServiceConfig {
    @ServiceProperty("service.redis.password")
    private String redisPassword;

    @ServiceProperty("service.redis.installPath")
    private String redisIntallPath;

    @ServiceProperty("service.redis.failoverInterface")
    private String failoverInterface;

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getRedisIntallPath() {
        return redisIntallPath;
    }

    public void setRedisIntallPath(String redisIntallPath) {
        this.redisIntallPath = redisIntallPath;
    }

    public String getFailoverInterface() {
        return failoverInterface;
    }

    public void setFailoverInterface(String failoverInterface) {
        this.failoverInterface = failoverInterface;
    }
}
