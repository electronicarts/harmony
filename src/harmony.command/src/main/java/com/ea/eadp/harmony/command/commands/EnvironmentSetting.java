/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.ea.eadp.harmony.command.annotation.CommandPath;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 4/27/2018.
 */
@Component
public class EnvironmentSetting {
    private boolean echo = false;

    public boolean isEcho() {
        return echo;
    }

    private String errorMsg = "Command format error. Should be \"set echo on|off\"";

    @Value("${harmony.env.application}")
    private String application;

    @Value("${harmony.env.universe}")
    private String universe;

    @Autowired
    private ZooKeeperConfig zkprConfig;

    @CommandPath(path = "/set/echo/<status>")
    public String setEcho(String status) {
        if (status.equalsIgnoreCase("on")) {
            echo = true;
            return "Command echo on.";
        }

        if (status.equalsIgnoreCase("off")) {
            echo = false;
            return "Command echo off.";
        }

        return errorMsg;
    }

    @CommandPath(path = "/get/application")
    public String getClusterApplication() {
        return application;
    }

    @CommandPath(path = "/get/universe")
    public String getClusterUniverse() {
        return universe;
    }

    @CommandPath(path = "/get/zkpr/connection_string")
    public String getZkprConnectionString() {
        return zkprConfig.getConnectionString();
    }
}
