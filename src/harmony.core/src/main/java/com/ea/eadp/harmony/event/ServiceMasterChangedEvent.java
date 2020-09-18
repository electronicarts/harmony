/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.event;

import com.ea.eadp.harmony.shared.event.HarmonyEvent;

/**
 * Created by juding on 10/10/16.
 */
public class ServiceMasterChangedEvent extends HarmonyEvent {
    private String service;
    private String node;

    public ServiceMasterChangedEvent() {
    }

    public ServiceMasterChangedEvent(String service, String node) {
        this.service = service;
        this.node = node;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }
}
