/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.configuration.properties.PropertySourceType;
import com.ea.eadp.harmony.shared.HarmonyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by leilin on 10/14/2014.
 */
public class ZipFilePropertyProviderFactory implements PropertyProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(ZipFilePropertyProviderFactory.class);

    private String configFile;

    public ZipFilePropertyProviderFactory(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public PropertyProvider createPropertyProvider() {
        return new ZipFilePropertyProvider();
    }

    public class ZipFilePropertyProvider implements PropertyProvider {
        private ZipFile zipFile = null;

        public ZipFilePropertyProvider() {
            try {
                zipFile = new ZipFile(new File(configFile));
            } catch (Exception ex) {
                throw new RuntimeException("Unable to open configuration file " + configFile, ex);
            }
        }

        @Override
        public void close() {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error closing zipfile", ex);
            }
        }

        @Override
        public HarmonyPropertySource getPropertySource(HarmonyConfigPath path) {
            return new HarmonyPropertySource(PropertySourceType.CONFIG_FILE, path, getProperties(path.getPath()));
        }

        private Properties getProperties(String path) {
            Properties properties = new Properties();
            String filePath = path.isEmpty() ? "default.properties" : path + "/default.properties";
            InputStream stream = null;
            try {
                ZipEntry zipEntry = zipFile.getEntry(filePath);
                if (zipEntry != null) {
                    stream = zipFile.getInputStream(zipEntry);
                }
                if (stream != null) {
                    properties.load(stream);
                    HarmonyUtils.logProperties(logger, properties, "Properties loaded from zip file {}", filePath);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to load properties from path [" + path + "]", ex);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    // ignored
                }
            }

            return properties;
        }
    }
}
