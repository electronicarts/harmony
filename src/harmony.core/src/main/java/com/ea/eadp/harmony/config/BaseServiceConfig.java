/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.config.annotation.ServiceProperty;

import java.util.List;

/**
 * Created by leilin on 10/15/2014.
 */

public abstract class BaseServiceConfig implements ServiceConfig {
    @ServiceProperty(value = "service.nodes")
    private List<String> allNodes;

    @ServiceProperty(value = "harmony.env.service")
    private String service;

    @ServiceProperty(value = "harmony.env.node")
    private String node;

    @ServiceProperty(value = "service.host")
    private String host;

    @ServiceProperty("service.port")
    private int port;

    @ServiceProperty("command.endpoint")
    private String commandEndpoint;

    @ServiceProperty("harmony.server.port")
    private int harmonyServerPort;

    public List<String> getAllNodes() {
        return allNodes;
    }

    public String getService() {
        return service;
    }

    public String getNode() {
        return node;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCommandEndpoint() {
        return commandEndpoint;
    }

    public void setCommandEndpoint(String commandEndpoint) {
        this.commandEndpoint = commandEndpoint;
    }

    public int getHarmonyServerPort() {
        return harmonyServerPort;
    }

    public void setHarmonyServerPort(int harmonyServerPort) {
        this.harmonyServerPort = harmonyServerPort;
    }
}
