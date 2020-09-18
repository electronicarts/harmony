/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.vip;

import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by juding on 11/5/2014.
 */
public class VipCommand extends HarmonyCommand {
    private VipAction action;

    public VipCommand() {
    }

    public VipCommand(ServiceEnvironment target, VipAction action) {
        super(target);
        this.action = action;
    }

    public VipCommand(VipAction action) {
        this.action = action;
    }

    public VipAction getAction() {
        return action;
    }

    public void setAction(VipAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "VipCommand{" + super.toString() +
                "action=" + action +
                '}';
    }
}
