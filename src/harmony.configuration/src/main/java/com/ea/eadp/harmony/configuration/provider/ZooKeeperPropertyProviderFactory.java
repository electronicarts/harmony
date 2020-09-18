/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.configuration.properties.PropertySourceType;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by leilin on 10/17/2014.
 */
public class ZooKeeperPropertyProviderFactory implements PropertyProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperPropertyProviderFactory.class);

    private ZooKeeperConfig zooKeeperConfig;
    private ZooKeeperService zooKeeperService;

    public ZooKeeperPropertyProviderFactory(ZooKeeperConfig zooKeeperConfig, ZooKeeperService zooKeeperService) {
        this.zooKeeperConfig = zooKeeperConfig;
        this.zooKeeperService = zooKeeperService;
    }

    @Override
    public PropertyProvider createPropertyProvider() {
        return new ZooKeeperPropertyProvider();
    }

    public class ZooKeeperPropertyProvider implements PropertyProvider {

        public ZooKeeperPropertyProvider() {
        }

        @Override
        public void close() {
        }

        @Override
        public HarmonyPropertySource getPropertySource(HarmonyConfigPath path) {
            try {
                logger.info("trying to get properties from zookeeper for path: {}", path);
                String configPath = ZKPaths.makePath(zooKeeperConfig.getDynamicPropertiesRoot(), path.getPath());
                String propertyFolderPath = ZKPaths.makePath(configPath, "_properties");
                logger.info("Checking zookeeper node:{}" , propertyFolderPath);
                if (zooKeeperService.checkExists(propertyFolderPath) == null) {
                    logger.info("Node not found:{}" , propertyFolderPath);
                    return empty(path);
                }

                Properties properties = new Properties();

                for (String key : zooKeeperService.getChildren(propertyFolderPath)) {
                    String propertyPath = ZKPaths.makePath(propertyFolderPath, key);
                    String value = zooKeeperService.getNodeStringData(propertyPath);
                    logger.info("Resolved property {}={} from zoo keeper", key, value);
                    properties.put(key, value);
                }

                return new HarmonyPropertySource(PropertySourceType.ZOO_KEEPER, path, properties);
            } catch (Exception ex) {
                // log error and return empty
                logger.error("Error getting properties from zookeeper for path: {}, ignoring zookeeper properties", path.getPath());
                return empty(path);
            }
        }

        public HarmonyPropertySource empty(HarmonyConfigPath path) {
            return new HarmonyPropertySource(PropertySourceType.ZOO_KEEPER, path, new Properties());
        }
    }
}
