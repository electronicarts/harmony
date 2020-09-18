/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;

/**
 * Created by leilin on 10/17/2014.
 */
public abstract class PropertyProviderWrapperFactory implements PropertyProviderFactory {
    private PropertyProviderFactory factory;

    protected PropertyProviderWrapperFactory(PropertyProviderFactory factory) {
        this.factory = factory;
    }

    @Override
    public PropertyProvider createPropertyProvider() {
        return new PropertyProviderWrapper(factory.createPropertyProvider());
    }

    protected abstract HarmonyPropertySource getPropertySource(HarmonyConfigPath path, PropertyProvider provider);


    public class PropertyProviderWrapper implements PropertyProvider {
        private PropertyProvider provider;

        public PropertyProviderWrapper(PropertyProvider provider) {
            this.provider = provider;
        }

        @Override
        public void close() {
            this.provider.close();
        }

        @Override
        public HarmonyPropertySource getPropertySource(HarmonyConfigPath path) {
            return PropertyProviderWrapperFactory.this.getPropertySource(path, provider);
        }
    }
}
