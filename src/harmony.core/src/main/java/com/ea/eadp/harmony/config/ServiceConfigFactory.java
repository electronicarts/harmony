/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

/**
 * Created by leilin on 10/16/2014.
 */
public interface ServiceConfigFactory<TConfig extends ServiceConfig> {
    BaseServiceConfig resolveServiceConfig(ServiceEnvironment serviceEnvironment);
}
