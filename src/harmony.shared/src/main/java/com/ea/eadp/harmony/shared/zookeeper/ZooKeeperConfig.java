/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.zookeeper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * User: leilin
 * Date: 10/6/14
 */
@Component
public class ZooKeeperConfig {

    @Value("${harmony.zookeeper.connectionString}")
    private String connectionString;

    @Value("${harmony.zookeeper.maxRetry}")
    private int maxRetry;

    @Value("${harmony.zookeeper.retrySleep}")
    private int retrySleep;

    @Value("${harmony.zookeeper.dynamicProperties.root}")
    private String dynamicPropertiesRoot;

    @Value("${harmony.zookeeper.dynamicProperties.enabled}")
    private boolean dynamicPropertiesEnabled;

    @Value("${harmony.zookeeper.appRoot}")
    private String appRoot;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getAppRoot() { return appRoot; }

    public void setAppRoot(String appRoot) {
        this.appRoot = appRoot;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getRetrySleep() {
        return retrySleep;
    }

    public void setRetrySleep(int retrySleep) {
        this.retrySleep = retrySleep;
    }

    public String getDynamicPropertiesRoot() {
        return dynamicPropertiesRoot;
    }

    public void setDynamicPropertiesRoot(String dynamicPropertiesRoot) {
        this.dynamicPropertiesRoot = dynamicPropertiesRoot;
    }

    public boolean isDynamicPropertiesEnabled() {
        return dynamicPropertiesEnabled;
    }

    public void setDynamicPropertiesEnabled(boolean dynamicPropertiesEnabled) {
        this.dynamicPropertiesEnabled = dynamicPropertiesEnabled;
    }
}
