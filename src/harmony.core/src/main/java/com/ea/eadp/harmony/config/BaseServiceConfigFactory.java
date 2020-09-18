/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.config.annotation.ServiceProperty;
import com.ea.eadp.harmony.configuration.configurationService.ConfigurationService;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.lang.reflect.Field;

/**
 * Created by leilin on 10/16/2014.
 */
public abstract class BaseServiceConfigFactory<TConfig extends BaseServiceConfig> implements ServiceConfigFactory<TConfig> {

    @Autowired
    private ConfigurationService configurationService;

    private Class<TConfig> configType;

    protected BaseServiceConfigFactory(Class<TConfig> configType) {
        this.configType = configType;
    }

    @Override
    public BaseServiceConfig resolveServiceConfig(ServiceEnvironment serviceEnvironment) {
        try {
            TConfig obj = configType.getConstructor().newInstance();

            // prepare properties
            PropertySources servicePropertySources = configurationService.getPropertySources(serviceEnvironment);
            PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(servicePropertySources);

            Class<?> type = configType;
            while (type != null) {
                for (Field field : type.getDeclaredFields()) {
                    ServiceProperty serviceProperty = field.getAnnotation(ServiceProperty.class);
                    if (serviceProperty != null) {
                        String propertyName = serviceProperty.value();
                        Class<?> propertyType = field.getType();
                        try {
                            Object propertyValue = resolver.getRequiredProperty(propertyName, propertyType);
                            field.setAccessible(true);
                            field.set(obj, propertyValue);
                        } catch (Exception ex) {
                            throw new RuntimeException("Failed to resolve property: " + propertyName, ex);
                        }
                    }
                }
                type = type.getSuperclass();
            }

            return obj;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
