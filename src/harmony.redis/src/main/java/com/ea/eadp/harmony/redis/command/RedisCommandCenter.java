/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.command;

import com.ea.eadp.harmony.command.CommandHandler;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.vip.VipCommandCenter;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.redis.entity.RedisAction;
import com.ea.eadp.harmony.redis.utils.RedisCommandWrapper;
import com.ea.eadp.harmony.redis.utils.RedisFailoverCommandWrapper;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Created by juding on 5/18/16.
 */
@Component
public class RedisCommandCenter extends VipCommandCenter {
    private final static Logger logger = LoggerFactory.getLogger(RedisCommandCenter.class);

    @CommandHandler(RedisCommand.class)
    public HarmonyCommandResult handleRedisCommand(RedisCommand command) {
        Assert.notNull(command);
        Assert.notNull(command.getAction());
        Assert.notNull(command.getTarget());

        ServiceEnvironment serviceEnv = command.getTarget();
        RedisServiceConfig redisServiceConfig = (RedisServiceConfig)serviceConfigRepository.getServiceConfig(serviceEnv);
        HarmonyCommandResult res = null;

        switch (command.getAction()) {
            case Kill_CONNECTIONS:
                RedisCommandWrapper cmd = new RedisCommandWrapper(redisServiceConfig);
                res = cmd.killRedisConnections();
                break;
            case CHANGE_MASTER:
                RedisFailoverCommand failoverCommand = (RedisFailoverCommand) command;
                RedisServiceConfig masterConfig = (RedisServiceConfig)serviceConfigRepository.getServiceConfig(((RedisFailoverCommand) command).getMasterEnv());
                RedisFailoverCommandWrapper failoverCommandWrapper = new RedisFailoverCommandWrapper(redisServiceConfig, masterConfig);
                res = failoverCommandWrapper.changeRedisMaster();
                break;
            case GET_REPLICATION_STATUS:
                RedisCommandWrapper redisCommand = new RedisCommandWrapper(redisServiceConfig);
                res = redisCommand.checkRedisReplication();
                break;
            default:
                throw new RuntimeException("Not a valid redis action:"+command.getAction());
        }

        return res;
    }

    @CommandHandler(RedisFailoverCommand.class)
    public HarmonyCommandResult handleRedisFailoverCommand(RedisCommand command) {
        Assert.notNull(command);
        Assert.notNull(command.getAction());
        Assert.isTrue(command.getAction()== RedisAction.CHANGE_MASTER);

        return handleRedisCommand(command);
    }
}