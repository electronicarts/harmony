/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.config;

/**
 * Created by leilin on 10/14/2014.
 */
public class ServiceEnvironment extends HarmonyEnvironment {
    private String service;

    public ServiceEnvironment() {
    }

    public ServiceEnvironment(HarmonyEnvironment environment, String service) {
        this.setApplication(environment.getApplication());
        this.setUniverse(environment.getUniverse());
        this.setClusterType(environment.getClusterType());
        this.setCluster(environment.getCluster());
        this.setNode(environment.getNode());
        this.setService(service);
    }

    public ServiceEnvironment(ServiceEnvironment environment) {
        this(environment, environment.getService());
    }


    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return String.format("{application=%s, universe=%s, clusterType=%s, cluster=%s, node=%s, service=%s}",
                application, universe, clusterType, cluster, node, service);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServiceEnvironment that = (ServiceEnvironment) o;

        if (service != null ? !service.equals(that.service) : that.service != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (service != null ? service.hashCode() : 0);
        return result;
    }
}
