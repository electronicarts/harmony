/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.shared.HarmonyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Created by leilin on 10/14/2014.
 */
public class FlattedProperyProviderFactory extends PropertyProviderWrapperFactory {
    private static final Logger logger = LoggerFactory.getLogger(FlattedProperyProviderFactory.class);

    private static final String FLATTED_PROPERTY_MARK = "._p_.";
    private static final String PATH_SEPERATOR = "/";
    private static final String PROPERTY_NAME_SEPERATOR = ".";


    private int maxFolderCount = 3;

    public FlattedProperyProviderFactory(PropertyProviderFactory factory) {
        super(factory);
    }

    public FlattedProperyProviderFactory(PropertyProviderFactory factory, int maxFolderCount) {
        super(factory);
        this.maxFolderCount = maxFolderCount;
    }

    @Override
    protected HarmonyPropertySource getPropertySource(HarmonyConfigPath path, PropertyProvider provider) {
        String[] folders = path.getPath().split(PATH_SEPERATOR);
        String propertyPrefix = null;
        String flattedPath = path.getPath();

        if (folders != null && folders.length > maxFolderCount) {
            // cut the first n part of the path
            flattedPath = StringUtils.join(folders, PATH_SEPERATOR, 0, maxFolderCount);

            // fold rest of the path as property prefix
            propertyPrefix = StringUtils.join(folders, PROPERTY_NAME_SEPERATOR, maxFolderCount, folders.length) + FLATTED_PROPERTY_MARK;
        }

        HarmonyPropertySource source = provider.getPropertySource(new HarmonyConfigPath(flattedPath));
        Properties properties = source.getProperties();

        Properties filtered = new Properties();
        if (propertyPrefix == null) {
            // remove all properties wit prefix
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (!key.contains(FLATTED_PROPERTY_MARK)) {
                    filtered.put(key, value);
                }
            }
        } else {
            // filter properties with prefix
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {

                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (key.startsWith(propertyPrefix)) {
                    filtered.put(key.substring(propertyPrefix.length()), value);
                }
            }
        }

        HarmonyUtils.logProperties(logger, filtered, "flatted properties loaded from path={}, flattedPath={}, propertyPrefix={} ", path, flattedPath, propertyPrefix);
        return new HarmonyPropertySource(source.getPropertySourceType(), path, filtered);
    }

}
