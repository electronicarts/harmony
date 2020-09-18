/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.command;

import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.redis.inspection.RedisReplicationRole;
import com.ea.eadp.harmony.redis.inspection.RedisReplicationStatusEntity;
import com.ea.eadp.harmony.redis.utils.StringConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.Assert;

public class RedisReplicationCommandResult extends HarmonyCommandResult{
    private RedisReplicationStatusEntity redisReplicationStatusEntity;

    public RedisReplicationStatusEntity getRedisReplicationStatusEntity() {
        return redisReplicationStatusEntity;
    }

    public void setRedisReplicationStatusEntity(RedisReplicationStatusEntity redisReplicationStatusEntity) {
        this.redisReplicationStatusEntity = redisReplicationStatusEntity;
    }

    @JsonIgnore
    public Boolean isSyncCompleted() {
        Assert.isTrue(redisReplicationStatusEntity.getRedisReplicationRole() == RedisReplicationRole.SLAVE);

        return redisReplicationStatusEntity.getRedisReplicationProperty(StringConstants.REPL_MASTER_SYNC_IN_PROGRESS).equals("0") &&
                redisReplicationStatusEntity.getRedisReplicationProperty(StringConstants.REPL_MASTER_LINK_STATUS).equals("up");
    }
}
