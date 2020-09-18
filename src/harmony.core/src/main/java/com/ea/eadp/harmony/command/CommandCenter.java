/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

/**
 * Created by leilin on 10/21/2014.
 */
public interface CommandCenter {
    // execute command either locally or remotely
    // exception will be thrown if error happened
    HarmonyCommandResult executeCommand(HarmonyCommand command);

    // execute command either locally or remotely
    // exception will be thrown if error happened if throwOnError=true
    // HarmonyCommandResult will be returned with FAILED if throwOnError=false
    HarmonyCommandResult executeCommand(HarmonyCommand command, boolean throwOnError);


    // execute command either locally or remotely
    // exception will be thrown if error happened if throwOnError=true
    // HarmonyCommandResult will be returned with FAILED if throwOnError=false
    HarmonyCommandResult executeCommand(HarmonyCommand command, boolean throwOnError, boolean localOnly);
}
