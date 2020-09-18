/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.configurationService;

import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySources;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by leilin on 10/17/2014.
 */
public class CompositeConfigurationService implements ConfigurationService {
    private List<ConfigurationService> configurationServices;

    public CompositeConfigurationService(ConfigurationService... configurationServices) {
        this.configurationServices = Arrays.asList(configurationServices);
    }

    @Override
    public HarmonyPropertySources getPropertySources() {
        HarmonyPropertySources propertySources = new HarmonyPropertySources();
        for (ConfigurationService service : configurationServices) {
            propertySources.addAll(service.getPropertySources(), true);
        }
        return propertySources;
    }

    @Override
    public HarmonyPropertySources getPropertySources(ServiceEnvironment serviceEnvironment) {
        HarmonyPropertySources propertySources = new HarmonyPropertySources();
        for (ConfigurationService service : configurationServices) {
            propertySources.addAll(service.getPropertySources(serviceEnvironment), true);
        }
        return propertySources;
    }
}
