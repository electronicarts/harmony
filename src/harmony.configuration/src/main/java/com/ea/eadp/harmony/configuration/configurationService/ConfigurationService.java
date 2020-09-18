/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.configurationService;

import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySources;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

public interface ConfigurationService {

    HarmonyPropertySources getPropertySources();

    HarmonyPropertySources getPropertySources(ServiceEnvironment serviceEnvironment);

}
