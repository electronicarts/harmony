/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.properties;

/**
 * Created by leilin on 10/17/2014.
 */
public class HarmonyConfigPath implements Comparable<HarmonyConfigPath> {
    private String path;

    public HarmonyConfigPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int compareTo(HarmonyConfigPath o) {
        return -path.compareTo(o.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HarmonyConfigPath that = (HarmonyConfigPath) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HarmonyConfigPath{" +
                "path='" + path + '\'' +
                '}';
    }
}
