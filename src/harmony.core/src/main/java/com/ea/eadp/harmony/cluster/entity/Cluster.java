/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;

import java.util.List;
import java.util.Map;

/**
 * User: leilin
 * Date: 10/2/14
 */
public abstract class Cluster {
    public String runnerCount;
    public List services;
    public String harmonyLeader;

    @ZKPRMapping(path="<<services>>/roles", valueType = Role.class)
    public Map<String, Role> roles;

    public abstract String checkServiceStatus(TemplateRenderer render);

    public abstract String checkServiceStatusInTable();

    public String getRunnerCount() {
        return runnerCount;
    }

    public List getServices() {
        return services;
    }

    public String getHarmonyLeader() {
        return harmonyLeader;
    }

    public Map<String, Role> getRoles() {
        return roles;
    }
}
