/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ClusterBuilder;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 3/5/2018.
 */
@Component
public class MySQLClusterBuilder extends ClusterBuilder{
    @Override
    public Cluster buildCluster(String zkprPath) throws InstantiationException, IllegalAccessException {
        return (Cluster) entityBuilder.createMappedObject(MySQLCluster.class, zkprPath);
    }
}
