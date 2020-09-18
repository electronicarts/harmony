/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.properties;

import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Created by leilin on 10/17/2014.
 */
public class HarmonyPropertySource extends PropertiesPropertySource implements Comparable<HarmonyPropertySource> {
    private PropertySourceType propertySourceType;
    private HarmonyConfigPath path;
    private Properties properties;

    public HarmonyPropertySource(PropertySourceType propertySourceType, HarmonyConfigPath path, Properties source) {
        super(propertySourceType.toString() + "_" + path.getPath(), source);
        this.propertySourceType = propertySourceType;
        this.path = path;
        this.properties = source;
    }

    public Properties getProperties() {
        return properties;
    }

    public PropertySourceType getPropertySourceType() {
        return propertySourceType;
    }

    public HarmonyConfigPath getPath() {
        return path;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public int compareTo(HarmonyPropertySource o) {
        // system level property is always highest priority
        if (this.propertySourceType == PropertySourceType.SYSTEM) {
            return -1;
        } else if (o.propertySourceType == PropertySourceType.SYSTEM) {
            return 1;
        }

        int path =  this.getPath().compareTo(o.getPath());
        return path == 0 ? this.propertySourceType.ordinal() - o.propertySourceType.ordinal() : path;
    }
}
