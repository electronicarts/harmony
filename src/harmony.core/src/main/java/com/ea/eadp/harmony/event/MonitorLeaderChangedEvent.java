/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.event;

import com.ea.eadp.harmony.shared.event.HarmonyEvent;

/**
 * User: leilin
 * Date: 10/8/14
 */
public class MonitorLeaderChangedEvent extends HarmonyEvent {
    private String clusterId;
    private boolean leader;

    public MonitorLeaderChangedEvent() {
    }

    public MonitorLeaderChangedEvent(String clusterId, boolean leader) {
        this.clusterId = clusterId;
        this.leader = leader;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }
}
