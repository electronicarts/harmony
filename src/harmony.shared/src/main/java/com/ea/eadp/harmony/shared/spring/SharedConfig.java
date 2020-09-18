/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * User: leilin
 * Date: 10/6/14
 */
@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = "com.ea.eadp.harmony.shared")
public class SharedConfig {
}
