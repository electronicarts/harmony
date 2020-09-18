/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;

/**
 * Created by VincentZhang on 5/2/2018.
 */
public class NodeConfig {
    @ZKPRMapping(path = "config/writerVip")
    public String writerVip;

    @ZKPRMapping(path = "config/hostname")
    public String hostName;

    @ZKPRMapping(path = "config/port")
    public String port;

    @ZKPRMapping(path = "config/harmonyServerPort")
    public String harmonyServerPort;
}
