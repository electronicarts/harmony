/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.HarmonyStatus;

import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;

/**
 * Created by VincentZhang on 5/4/2018.
 */
public class HarmonyName {
    @ZKPRMapping(path="")
    public String nodeName;

    public String getNodeName() {
        return nodeName;
    }
}
