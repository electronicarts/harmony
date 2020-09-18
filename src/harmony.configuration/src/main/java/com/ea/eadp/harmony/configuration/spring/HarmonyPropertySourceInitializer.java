/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.spring;

import com.ea.eadp.harmony.configuration.configurationService.ConfigurationService;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by leilin on 10/17/2014.
 */
public class HarmonyPropertySourceInitializer implements ApplicationContextInitializer {
    private ConfigurationService configurationService;

    public HarmonyPropertySourceInitializer(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurationService service = this.configurationService;

        if (applicationContext.getParent() != null) {
            // initializing child application
            // use dynamic configuration service created by parent application
            service = applicationContext.getParent().getBean(ConfigurationService.class);
        }

        MutablePropertySources applicationPropertySources = applicationContext.getEnvironment().getPropertySources();
        ArrayList<PropertySource> sources = new ArrayList<PropertySource>();
        for (PropertySource propertySource : service.getPropertySources()) {
           sources.add(propertySource);
        }
        Collections.reverse(sources);
        for (PropertySource propertySource : sources) {
            applicationPropertySources.addFirst(propertySource);
        }
    }
}
