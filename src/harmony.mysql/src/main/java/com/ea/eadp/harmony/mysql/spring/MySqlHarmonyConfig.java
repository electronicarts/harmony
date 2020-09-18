/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.spring;

/**
 * Created by leilin on 10/15/2014.
 */

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.ea.eadp.harmony.mysql")
public class MySqlHarmonyConfig {
}
