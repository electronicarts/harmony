/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.config;

/**
 * User: leilin
 * Date: 10/6/14
 */
public class HarmonyEnvironment {
    protected String application = "default";

    protected String universe = "default";

    protected String clusterType = "default";

    protected String cluster = "default";

    protected String node = "default";

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUniverse() {
        return universe;
    }

    public void setUniverse(String universe) {
        this.universe = universe;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public HarmonyEnvironment() {
    }

    public HarmonyEnvironment(HarmonyEnvironment environment) {
        this.setApplication(environment.getApplication());
        this.setUniverse(environment.getUniverse());
        this.setClusterType(environment.getClusterType());
        this.setCluster(environment.getCluster());
        this.setNode(environment.getNode());
    }

    @Override
    public String toString() {
        return String.format("{application=%s, universe=%s, clusterType=%s, cluster=%s, node=%s}",
                application, universe, clusterType, cluster, node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HarmonyEnvironment that = (HarmonyEnvironment) o;

        if (application != null ? !application.equals(that.application) : that.application != null) return false;
        if (cluster != null ? !cluster.equals(that.cluster) : that.cluster != null) return false;
        if (clusterType != null ? !clusterType.equals(that.clusterType) : that.clusterType != null) return false;
        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (universe != null ? !universe.equals(that.universe) : that.universe != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        result = 31 * result + (universe != null ? universe.hashCode() : 0);
        result = 31 * result + (clusterType != null ? clusterType.hashCode() : 0);
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }
}
