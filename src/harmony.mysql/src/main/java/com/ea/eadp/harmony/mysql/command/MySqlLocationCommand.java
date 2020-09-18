/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.mysql.entity.MasterLocation;
import com.ea.eadp.harmony.mysql.entity.MySqlAction;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by juding on 10/30/2014.
 */
public class MySqlLocationCommand extends MySqlCommand {
    private MasterLocation location;

    public MySqlLocationCommand() {
    }

    public MySqlLocationCommand(ServiceEnvironment target, MySqlAction action, MasterLocation location) {
        super(target, action);
        this.location = location;
    }

    public MySqlLocationCommand(MasterLocation location) {
        this.location = location;
    }

    public MasterLocation getLocation() {
        return location;
    }

    public void setLocation(MasterLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "MySqlLocationCommand{" + super.toString() +
                "location=" + location +
                '}';
    }
}
