/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.configuration.properties.PropertySourceType;

import java.util.Properties;

/**
 * Created by leilin on 10/17/2014.
 */
public class DummyPropertyProviderFactory implements PropertyProviderFactory, PropertyProvider {
    @Override
    public void close() {

    }

    @Override
    public HarmonyPropertySource getPropertySource(HarmonyConfigPath path) {
        Properties properties = new Properties();
        properties.put("test", "test");
        return new HarmonyPropertySource(PropertySourceType.ZOO_KEEPER, path, properties);
    }

    @Override
    public PropertyProvider createPropertyProvider() {
        return this;
    }
}
