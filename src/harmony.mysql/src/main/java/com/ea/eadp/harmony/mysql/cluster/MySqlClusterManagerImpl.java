/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

import com.ea.eadp.harmony.cluster.ClusterManagerImpl;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.stereotype.Component;

/**
 * Created by juding on 11/14/14.
 */
@Component
public class MySqlClusterManagerImpl extends ClusterManagerImpl {
    protected void enhanceServicePath(String service, String path, ZooKeeperService zkSvc) {
        MySqlServiceConfig config = (MySqlServiceConfig) serviceConfigRepository.getServiceConfig(service);
        super.enhanceServicePath(path, zkSvc,config);
    }

    protected void enhanceNodePath(String service, String node, String path, ZooKeeperService zkSvc) {
        super.enhanceNodePath(service, node, path, zkSvc);

        String atPath = path + "/properties";
        zkSvc.ensurePath(atPath);
        zkSvc.ensurePath(atPath + "/secondsBehindMaster");
        zkSvc.ensurePath(atPath + "/zxidOfMaster");
    }
}
