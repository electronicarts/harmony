/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest;

import com.ea.eadp.harmony.cluster.entity.Cluster;

import java.util.List;

/**
 * Created by VincentZhang on 4/4/2018.
 */

public interface ClusterService {
    List<String> getAllShards();

    Cluster getCluster(String clusterName);
}
