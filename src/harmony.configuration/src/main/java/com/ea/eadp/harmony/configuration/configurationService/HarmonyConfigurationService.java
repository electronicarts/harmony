/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.configurationService;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySources;
import com.ea.eadp.harmony.configuration.properties.PropertySourceType;
import com.ea.eadp.harmony.configuration.provider.PropertyProvider;
import com.ea.eadp.harmony.configuration.provider.PropertyProviderFactory;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by leilin on 10/13/2014.
 */
public class HarmonyConfigurationService implements ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(HarmonyConfigurationService.class);

    private static final String PATH_PATTERNS[] = {
            "",
            "${application}",
            "${application}/${universe}",
            "${application}/${universe}/${clusterType}",
            "${application}/${universe}/${clusterType}/${cluster}",
            "${application}/${universe}/${clusterType}/${cluster}/${node}",
    };

    private static final String PATH_PATTERNS_WITH_SERVICE[] = {
            "",
            "${application}",
            "${application}/${universe}",
            "${application}/${universe}/${clusterType}",
            "${application}/${universe}/${clusterType}/_service-${service}",
            "${application}/${universe}/${clusterType}/${cluster}",
            "${application}/${universe}/${clusterType}/${cluster}/_service-${service}",
            "${application}/${universe}/${clusterType}/${cluster}/${node}",
            "${application}/${universe}/${clusterType}/${cluster}/${node}/_service-${service}",
    };


    private PropertyProviderFactory providerFactory;

    private HarmonyEnvironment environment;

    private Properties propertiesOverride;

    public HarmonyConfigurationService(PropertyProviderFactory providerFactory, HarmonyEnvironment environment, Properties propertiesOverride) {
        this.providerFactory = providerFactory;
        this.environment = environment;
        this.propertiesOverride = propertiesOverride;
    }

    @Override
    public HarmonyPropertySources getPropertySources() {
        return getPropertySources(new ServiceEnvironment(this.environment, null));
    }

    @Override
    public HarmonyPropertySources getPropertySources(ServiceEnvironment env) {
        String[] pathTemplates = env.getService() == null ? PATH_PATTERNS : PATH_PATTERNS_WITH_SERVICE;

        HarmonyPropertySources propertySources = new HarmonyPropertySources();

        // replace tokens
        List<HarmonyConfigPath> paths = new ArrayList<HarmonyConfigPath>();
        for (String template : pathTemplates) {
            String path = template
                    .replace("${application}", env.getApplication())
                    .replace("${universe}", env.getUniverse())
                    .replace("${clusterType}", env.getClusterType())
                    .replace("${cluster}", env.getCluster())
                    .replace("${node}", env.getNode());
            if (env.getService() != null) {
                path = path.replace("${service}", env.getService());
            }
            paths.add(new HarmonyConfigPath(path));
        }

        PropertyProvider provider = providerFactory.createPropertyProvider();

        try {
            for (HarmonyConfigPath path : paths) {
                HarmonyPropertySource loadedProperties = provider.getPropertySource(path);
                if (loadedProperties != null && !loadedProperties.isEmpty()) {
                    propertySources.add(loadedProperties);
                }
            }
        } finally {
            try {
                provider.close();
            } catch (Exception ex) {
                logger.error("Error close property provider", ex);
            }
        }

        // override properties
        Properties properties = new Properties();
        if (propertiesOverride != null) {
            properties.putAll(propertiesOverride);
        }

        // override env properties
        properties.put("harmony.env.application", env.getApplication());
        properties.put("harmony.env.universe", env.getUniverse());
        properties.put("harmony.env.clusterType", env.getClusterType());
        properties.put("harmony.env.cluster", env.getCluster());
        properties.put("harmony.env.node", env.getNode());

        if (env.getService() != null) {
            properties.put("harmony.env.service", env.getService());
        } else {
            properties.remove("harmony.env.service");
        }

        HarmonyPropertySource propertySource = new HarmonyPropertySource(PropertySourceType.SYSTEM, new HarmonyConfigPath(""), properties);
        propertySources.add(propertySource);

        return propertySources;
    }
}
