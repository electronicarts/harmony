/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import org.junit.Test;
import org.springframework.core.env.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by leilin on 10/15/2014.
 */
public class TestPropertySourcesPropertyResolver {
    @Test
    public void test(){
        Properties properties = new Properties();
        properties.put("test.intValue", "12345");
        properties.put("test.stringValue", "test");
        properties.put("test.longValue", "123456789012345");
        properties.put("test.listValue", "abc, def, haha");
        PropertySource source = new PropertiesPropertySource("test", properties);
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(source);
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);
        String[] list = resolver.getProperty("test.listValue", String[].class);


        List<String> testValue = new ArrayList<String>();

        Object list2 = resolver.getProperty("test.listValue", testValue.getClass());
    }
}
