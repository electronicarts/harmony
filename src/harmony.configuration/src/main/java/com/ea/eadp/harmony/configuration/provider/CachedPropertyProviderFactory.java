/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by leilin on 10/14/2014.
 */
public class CachedPropertyProviderFactory implements PropertyProviderFactory{
    private PropertyProviderFactory factory;
    private ConcurrentHashMap<HarmonyConfigPath, HarmonyPropertySource> cache = new ConcurrentHashMap<HarmonyConfigPath, HarmonyPropertySource>();

    public CachedPropertyProviderFactory(PropertyProviderFactory factory) {
        this.factory = factory;
    }

    @Override
    public PropertyProvider createPropertyProvider() {
        return new CachedPropertyProvider();
    }


    public class CachedPropertyProvider implements PropertyProvider {
        private PropertyProvider provider = null;

        @Override
        public HarmonyPropertySource getPropertySource(HarmonyConfigPath path) {
            if (cache.containsKey(path)) {
                return cache.get(path);
            }

            if (provider == null) {
                synchronized (this) {
                    if (provider == null) {
                        provider = factory.createPropertyProvider();
                    }
                }
            }

            HarmonyPropertySource properties = provider.getPropertySource(path);
            cache.putIfAbsent(path, properties);
            return properties;
        }


        @Override
        public void close() {
            if (provider != null) {
                provider.close();
            }
        }
    }
}
