/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by leilin on 10/21/2014.
 */
public class PingCommand extends HarmonyCommand {
    private String message;

    public PingCommand() {
    }

    public PingCommand(ServiceEnvironment target, String message) {
        super(target);
        this.message = message;
    }

    public PingCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
