/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest.impl;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.rest.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by VincentZhang on 4/4/2018.
 */
@Service("clusterService")
public class ClusterServiceImpl implements ClusterService {
    @Autowired
    private ClusterManager clusterManager;

    @Override
    public List<String> getAllShards() {
        return clusterManager.getAllShards();
    }

    @Override
    public Cluster getCluster(String clusterName){
        return clusterManager.getClusterInformation(clusterName);
    }
}
