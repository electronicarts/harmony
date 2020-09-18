/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.cluster;

import com.ea.eadp.harmony.cluster.entity.EntityNode;

/**
 * Created by VincentZhang on 5/9/2018.
 */
public class RedisNode extends EntityNode{
    public RedisProperties properties;

    public RedisProperties getProperties(){
        return properties;
    }
}
