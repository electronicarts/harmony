/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.mysql.entity.MasterStatusDB;

/**
 * Created by juding on 10/31/2014.
 */
public class MasterStatusResponse extends HarmonyCommandResult {
    private MasterStatusDB status;

    public MasterStatusResponse() {
    }

    public MasterStatusResponse(MasterStatusDB status) {
        this.status = status;
    }

    public MasterStatusDB getStatus() {
        return status;
    }

    public void setStatus(MasterStatusDB status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MasterStatusResponse{" + super.toString() +
                "status=" + status +
                '}';
    }
}
