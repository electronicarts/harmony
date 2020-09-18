/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.HarmonyStatus;

import java.util.Map;

/**
 * Created by VincentZhang on 5/4/2018.
 */
public class HarmonyNodeStatus {
    public String lastClusterCheckTime;
    public String lastNodeInspectionTime;
    // Service Name to Vip Status
    public Map<String,HarmonyVipStatus> writerVipStatus;
    public Map<String,HarmonyVipStatus> readerVipStatus;

    public String getLastClusterCheckTime() {
        return lastClusterCheckTime;
    }

    public String getLastNodeInspectionTime() {
        return lastNodeInspectionTime;
    }

    public Map<String, HarmonyVipStatus> getwriterVipStatus() {
        return writerVipStatus;
    }

    public Map<String, HarmonyVipStatus> getreaderVipStatus() {
        return readerVipStatus;
    }
}
