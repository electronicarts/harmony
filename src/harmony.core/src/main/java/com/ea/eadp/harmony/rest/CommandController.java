/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest;

import com.ea.eadp.harmony.command.CommandCenter;
import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: leilin
 * Date: 10/7/14
 */
@RestController
public class CommandController {
    @Autowired
    private CommandCenter commandCenter;

    @RequestMapping(value = "/commands", method = RequestMethod.POST)
    public HarmonyCommandResult executeCommand(@Validated @RequestBody HarmonyCommand command) {
        return commandCenter.executeCommand(command, true);
    }
}
