/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.configurationService;

import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySources;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import org.springframework.core.env.PropertySources;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class CachedConfigurationService implements ConfigurationService {

    private HarmonyPropertySources defaultConfigCache = null;

    private final ConcurrentHashMap<ServiceEnvironment, HarmonyPropertySources> cache = new ConcurrentHashMap<ServiceEnvironment, HarmonyPropertySources>();

    private ConfigurationService configurationService;

    public CachedConfigurationService(ConfigurationService configurationService) {
        if (configurationService == null) {
            throw new IllegalArgumentException("configurationService is null");
        }

        this.configurationService = configurationService;
    }

    @Override
    public HarmonyPropertySources getPropertySources() {
        if (defaultConfigCache != null) {
            return defaultConfigCache;
        }

        synchronized (this) {
            if (defaultConfigCache != null) {
                return defaultConfigCache;
            }

            defaultConfigCache = configurationService.getPropertySources();
            return defaultConfigCache;
        }
    }

    @Override
    public HarmonyPropertySources getPropertySources(ServiceEnvironment serviceEnvironment) {
        if (serviceEnvironment == null) {
            throw new IllegalArgumentException("serviceEnvironment is null.");
        }

        HarmonyPropertySources properties = cache.get(serviceEnvironment);
        if (properties != null) {
            return properties;
        }

        synchronized (cache) {
            properties = cache.get(serviceEnvironment);
            if (properties != null) {
                return properties;
            }

            properties = configurationService.getPropertySources(serviceEnvironment);
            cache.put(serviceEnvironment, properties);

            return properties;
        }
    }
}
