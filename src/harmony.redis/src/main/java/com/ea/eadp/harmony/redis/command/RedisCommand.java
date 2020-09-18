/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.command;

import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.redis.entity.RedisAction;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

public class RedisCommand extends HarmonyCommand {
    protected RedisAction action;

    public RedisCommand(){}

    public RedisCommand(ServiceEnvironment target, RedisAction action) {
        super(target);
        this.action = action;
    }

    public RedisAction getAction() {
        return action;
    }

    public void setAction(RedisAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "RedisCommand{" + super.toString() +
                "action=" + action +
                '}';
    }
}
