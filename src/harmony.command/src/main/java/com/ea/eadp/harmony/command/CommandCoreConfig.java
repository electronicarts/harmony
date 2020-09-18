/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by VincentZhang on 4/27/2018.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.ea.eadp.harmony.cluster",
        "com.ea.eadp.harmony.command",
        "com.ea.eadp.harmony.config",
        "com.ea.eadp.harmony.monitor"
})
public class CommandCoreConfig {
}
