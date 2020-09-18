/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.utils;

import com.ea.eadp.harmony.redis.inspection.RedisReplicationStatusEntity;

public class RedisReplicationStatusEntityConverter {
    public static RedisReplicationStatusEntity convert(String result) {
        RedisReplicationStatusEntity ret = new RedisReplicationStatusEntity();
        if (result !=  null) {
            String[] entries = result.split("\n");
            for (String entry:entries) {
                String[] pair = entry.split(":");
                if (pair.length == 2) {
                    ret.setRedisReplicationProperty(pair[0],pair[1]);
                }
            }
        }

        return ret;
    }
}
