/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.mysql.entity.MySqlAction;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by juding on 10/30/2014.
 */
public class MySqlCommand extends HarmonyCommand {
    private MySqlAction action;

    public MySqlCommand() {
    }

    public MySqlCommand(ServiceEnvironment target, MySqlAction action) {
        super(target);
        this.action = action;
    }

    public MySqlCommand(MySqlAction action) {
        this.action = action;
    }

    public MySqlAction getAction() {
        return action;
    }

    public void setAction(MySqlAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "MySqlCommand{" + super.toString() +
                "action=" + action +
                '}';
    }
}
