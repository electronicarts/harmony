/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.app;

import com.ea.eadp.harmony.configuration.spring.HarmonyPropertyConfiguration;
import com.ea.eadp.harmony.configuration.spring.HarmonyPropertySourceInitializer;
import com.ea.eadp.harmony.shared.event.EventServiceRegisterListener;
import com.ea.eadp.harmony.spring.CoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties
@Import({CoreConfig.class})
@EnableAutoConfiguration
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws ClassNotFoundException {
        // init configuration Service
        HarmonyPropertyConfiguration.initializeConfigurationService();

        // resolve extra configuration
        String pluginType = HarmonyPropertyConfiguration.resolveProperty("harmony.spring.configType");
        Class<?> configType = Class.forName(pluginType);

        // init HarmonyPropertySourceInitializer
        HarmonyPropertySourceInitializer propertySourceInitializer = new HarmonyPropertySourceInitializer(HarmonyPropertyConfiguration.getConfigurationService());

        SpringApplicationBuilder builder = new SpringApplicationBuilder().sources(BaseApplication.class).initializers(propertySourceInitializer);

        SpringApplicationBuilder childBuilder = builder.child(Application.class, configType).initializers(propertySourceInitializer)
                .listeners(new EventServiceRegisterListener());

        try {
            // start application
            childBuilder.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Main function exited. Note: if other threads are still running, process won't quit!");
    }
}
