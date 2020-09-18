/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

/**
 * Created by leilin on 10/21/2014.
 */
public class CommandExecutionFailedException extends RuntimeException {
    public CommandExecutionFailedException(String message) {
        super(message);
    }

    public CommandExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
