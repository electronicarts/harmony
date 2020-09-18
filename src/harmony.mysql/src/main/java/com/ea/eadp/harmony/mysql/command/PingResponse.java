/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.command.HarmonyCommandResult;

/**
 * Created by leilin on 10/21/2014.
 */
public class PingResponse extends HarmonyCommandResult {
    private String message;

    public PingResponse() {
    }

    public PingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
