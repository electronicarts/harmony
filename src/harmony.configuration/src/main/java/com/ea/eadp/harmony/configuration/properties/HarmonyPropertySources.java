/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.properties;

import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.*;

/**
 * Created by leilin on 10/17/2014.
 */
public class HarmonyPropertySources implements PropertySources {
    private Map<String, HarmonyPropertySource> map = new HashMap<String, HarmonyPropertySource>();
    private List<HarmonyPropertySource> list = new ArrayList<HarmonyPropertySource>();

    @Override
    public boolean contains(String name) {
        return map.containsKey(name);
    }

    @Override
    public PropertySource<?> get(String name) {
        return map.get(name);
    }

    public void add(HarmonyPropertySource propertySource) {
        String name = propertySource.getName();
        if (map.containsKey(name)) {
            throw new IllegalArgumentException("source with name " + name + " already exists");
        }

        map.put(name, propertySource);

        // binary search and add propertySource
        int index = Collections.binarySearch(this.list, propertySource);
        if (index < 0) {
            index = -(index + 1);
        }
        list.add(index, propertySource);
    }

    public void addAll(HarmonyPropertySources propertySources, boolean ignoreDuplication) {
        for (HarmonyPropertySource source : propertySources.list) {
            if (ignoreDuplication && map.containsKey(source.getName())) {
                continue;
            }
            add(source);
        }
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return Collections.unmodifiableList(new ArrayList<PropertySource<?>>(list)).iterator();
    }
}
