/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * User: leilin
 * Date: 10/8/14
 */
@Configuration
@ComponentScan(basePackages = {
        "com.ea.eadp.harmony.cluster",
        "com.ea.eadp.harmony.config",
        "com.ea.eadp.harmony.control",
        "com.ea.eadp.harmony.event",
        "com.ea.eadp.harmony.inspection",
        "com.ea.eadp.harmony.monitor",
        "com.ea.eadp.harmony.rest",
        "com.ea.eadp.harmony.service",
        "com.ea.eadp.harmony.warmup"
})
public class CoreConfig {
}
