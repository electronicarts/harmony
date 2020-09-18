/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

/**
 * User: leilin
 * Date: 10/8/14
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class HarmonyEvent {
    private UUID eventId =  UUID.randomUUID();
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
}
