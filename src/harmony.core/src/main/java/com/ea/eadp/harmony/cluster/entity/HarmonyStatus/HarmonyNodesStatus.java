/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.HarmonyStatus;

import java.util.Map;

/**
 * Created by VincentZhang on 5/4/2018.
 */
public class HarmonyNodesStatus {

    // HarmonyNodeName --> status
    public Map<String, HarmonyNodeStatus> harmonyNodes;
    public String harmonyLeader;

    // uuid --> HarmonyNodeName
    public Map<String, HarmonyName> leader;

    public Map<String, HarmonyNodeStatus> getHarmonyNodes() {
        return harmonyNodes;
    }

    public Map<String, HarmonyName> getLeader() {
        return leader;
    }

    public String getHarmonyLeader() {
        return harmonyLeader;
    }
}
