/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.mysql.entity.MasterLocation;
import com.ea.eadp.harmony.mysql.entity.MySqlAction;
import com.ea.eadp.harmony.mysql.entity.StartSlaveUntil;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by juding on 10/31/2014.
 */
public class StartSlaveCommand extends MySqlLocationCommand {
    private StartSlaveUntil until;

    public StartSlaveCommand() {
    }

    public StartSlaveCommand(ServiceEnvironment target, MySqlAction action, MasterLocation location, StartSlaveUntil until) {
        super(target, action, location);
        this.until = until;
    }

    public StartSlaveCommand(StartSlaveUntil until) {
        this.until = until;
    }

    public StartSlaveUntil getUntil() {
        return until;
    }

    public void setUntil(StartSlaveUntil until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return "StartSlaveCommand{" + super.toString() +
                "until=" + until +
                '}';
    }
}
