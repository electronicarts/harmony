/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

/**
 * Created by leilin on 10/21/2014.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class HarmonyCommand {
    private UUID commandId = UUID.randomUUID();

    private ServiceEnvironment target;

    protected HarmonyCommand() {
    }

    protected HarmonyCommand(ServiceEnvironment target) {
        this.target = target;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public void setCommandId(UUID commandId) {
        this.commandId = commandId;
    }

    public ServiceEnvironment getTarget() {
        return target;
    }

    public void setTarget(ServiceEnvironment target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "HarmonyCommand{" +
                "commandId=" + commandId +
                ", target=" + target +
                '}';
    }
}
