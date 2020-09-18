/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.configuration.spring.HarmonyPropertyConfiguration;
import com.ea.eadp.harmony.shared.spring.SharedConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * User: VincentZhang
 * Date: 4/20/2018
 */
@Configuration
@Import({HarmonyPropertyConfiguration.class, SharedConfig.class})
public class BaseApplication {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}
