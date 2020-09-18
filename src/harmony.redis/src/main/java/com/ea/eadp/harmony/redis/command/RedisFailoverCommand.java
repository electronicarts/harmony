/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.command;

import com.ea.eadp.harmony.redis.entity.RedisAction;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import org.springframework.util.Assert;

public class RedisFailoverCommand extends RedisCommand{
    private ServiceEnvironment masterEnv;

    public RedisFailoverCommand() {}

    public RedisFailoverCommand(ServiceEnvironment target, ServiceEnvironment masterEnv) {
        super(target, RedisAction.CHANGE_MASTER);
        this.masterEnv = masterEnv;
    }

    public ServiceEnvironment getMasterEnv() {
        return masterEnv;
    }

    public void setMasterEnv(ServiceEnvironment masterEnv) {
        this.masterEnv = masterEnv;
    }
}
