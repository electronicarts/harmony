/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;

import java.util.List;
import java.util.Map;

/**
 * Created by VincentZhang on 5/2/2018.
 */
public class ClusterConfig {
    public List services;
    @ZKPRMapping(path = "<<services>>", valueType = ServiceConfig.class)
    public Map<String, ServiceConfig> serviceNodes;

    public ClusterProperties properties;
}
