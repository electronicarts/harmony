/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.cluster;

import java.util.Map;

/**
 * Created by VincentZhang on 5/9/2018.
 */
public class RedisNodes{
    public Map<String, RedisNode> nodes;

    public Map<String, RedisNode> getNodes(){
        return nodes;
    }
}
