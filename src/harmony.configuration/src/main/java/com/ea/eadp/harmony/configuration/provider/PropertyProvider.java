/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;

/**
 * Created by leilin on 10/14/2014.
 */
public interface PropertyProvider {
    void close();

    HarmonyPropertySource getPropertySource(HarmonyConfigPath path);
}
