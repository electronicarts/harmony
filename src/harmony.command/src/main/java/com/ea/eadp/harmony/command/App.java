/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.configuration.spring.HarmonyPropertyConfiguration;
import com.ea.eadp.harmony.configuration.spring.HarmonyPropertySourceInitializer;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import net.sourceforge.argparse4j.ArgumentParsers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by VincentZhang on 4/20/2018.
 */
@Configuration
@EnableConfigurationProperties
@EnableAutoConfiguration
@SpringBootApplication
@Import({CommandCoreConfig.class})
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws ClassNotFoundException {
        try{
            parseArguments(args);
            // init configuration Service
            HarmonyPropertyConfiguration.initializeConfigurationService();

            // resolve extra configuration
            String pluginType = HarmonyPropertyConfiguration.resolveProperty("harmony.spring.configType");
            Class<?> configType = Class.forName(pluginType);

            // init HarmonyPropertySourceInitializer
            HarmonyPropertySourceInitializer propertySourceInitializer = new HarmonyPropertySourceInitializer(HarmonyPropertyConfiguration.getConfigurationService());
            SpringApplicationBuilder builder = new SpringApplicationBuilder().sources(BaseApplication.class).initializers(propertySourceInitializer);
            SpringApplicationBuilder childBuilder = builder.
                    child(App.class, configType).initializers(propertySourceInitializer);

            // Running in non-interactive mode, no need to show banner
            if(null != System.getProperty("harmony.command")){
                childBuilder.bannerMode(Banner.Mode.OFF);
            }
            childBuilder.run(args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void parseArguments(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("harmony.command").build()
                .defaultHelp(true)
                .description("Harmony command line interface");
        parser.addArgument("-c", "--conf").help("Specify conf folder");
        parser.addArgument("-e", "--execute").help("Command to be executed. " +
                "If not specified, will under interactive command line tool");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        if(ns.<String> get("conf") != null){
            logger.debug("Executing using conf file:" + ns.<String> getString("conf"));
            System.setProperty(HarmonyPropertyConfiguration.CONF_JARFILE_PROPERTY, ns.<String> get("conf"));
        }

        if(ns.<String> get("execute") != null){
            logger.debug("Execute command:" + ns.<String> getString("execute"));
            System.setProperty(CommandProcessor.HARMONY_COMMAND_KEY, ns.<String> getString("execute"));
        }
    }
}
