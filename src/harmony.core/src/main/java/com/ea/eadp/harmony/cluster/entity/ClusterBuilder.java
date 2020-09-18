/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import com.ea.eadp.harmony.cluster.entity.HarmonyStatus.HarmonyNodesStatus;
import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.EntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by VincentZhang on 5/4/2018.
 */
public abstract class ClusterBuilder{
    @Autowired
    protected EntityBuilder entityBuilder;

    public abstract Cluster buildCluster(String targetClusterPath) throws InstantiationException, IllegalAccessException;

    public ClusterConfig buildClusterConfig(String zkprPath) throws InstantiationException, IllegalAccessException {
        return (ClusterConfig) entityBuilder.createMappedObject(ClusterConfig.class, zkprPath);
    }

    public HarmonyNodesStatus buildHarmonyNodesStatus(String zkprPath) throws InstantiationException, IllegalAccessException {
        return (HarmonyNodesStatus) entityBuilder.createMappedObject(HarmonyNodesStatus.class, zkprPath);
    }

    public ServiceProperties buildAutoFailoverConfig(String zkprPath) throws InstantiationException, IllegalAccessException {
        return (ServiceProperties) entityBuilder.createMappedObject(ServiceProperties.class, zkprPath);
    }
}
