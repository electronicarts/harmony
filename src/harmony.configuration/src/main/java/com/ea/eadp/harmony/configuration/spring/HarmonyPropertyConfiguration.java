/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.spring;

import com.ea.eadp.harmony.configuration.configurationService.CompositeConfigurationService;
import com.ea.eadp.harmony.configuration.configurationService.ConfigurationService;
import com.ea.eadp.harmony.configuration.configurationService.HarmonyConfigurationService;
import com.ea.eadp.harmony.configuration.crypto.CryptoService;
import com.ea.eadp.harmony.configuration.crypto.CryptoServiceImpl;
import com.ea.eadp.harmony.configuration.crypto.DummyCryptoServiceImpl;
import com.ea.eadp.harmony.configuration.crypto.cipher.impl.*;
import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import com.ea.eadp.harmony.configuration.provider.*;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * User: leilin
 * Date: 10/6/14
 */
@Configuration
@EnableConfigurationProperties
public class HarmonyPropertyConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(HarmonyPropertyConfiguration.class);

    private static HarmonyEnvironment harmonyEnvironment;
    private static ConfigurationService staticConfigurationService;

    private static final String DEFAULT = "default";

    public static String CONF_JARFILE_PROPERTY = "harmony.conf.jarfile";

    public static String resolveProperty(String key) {
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(staticConfigurationService.getPropertySources());
        return resolver.getRequiredProperty(key);
    }

    public static ConfigurationService getConfigurationService() {
        return staticConfigurationService;
    }

    public static void initializeConfigurationService() {
        String baseDir = System.getProperty("user.dir");

        // default environment
        harmonyEnvironment = new HarmonyEnvironment();

        // init configFile based configuration service
        File file = new File(baseDir, "conf/harmony-override.properties");
        String configurationFile = baseDir + "/conf/configuration-current.jar";

        String confJarFileLocation = System.getProperty(CONF_JARFILE_PROPERTY);
        if (confJarFileLocation != null && confJarFileLocation.length() > 0) {
            configurationFile = confJarFileLocation;
        }

        String cryptoMode = null;
        Boolean fallBackEnabled = false;

        // init basic config from override
        if (!file.exists()) {
            throw new RuntimeException("Config file " + file.getAbsolutePath() + " doesn't exist");
        }

        Properties overrideProperties = new Properties();

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file.getPath());
            overrideProperties.load(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // ignore;
                }
            }
        }

        String cluster = overrideProperties.getProperty("harmony.env.cluster", DEFAULT);
        String node = overrideProperties.getProperty("harmony.env.node", DEFAULT);
        String application = overrideProperties.getProperty("harmony.env.application", DEFAULT);
        String universe = overrideProperties.getProperty("harmony.env.universe", DEFAULT);
        String clusterType = overrideProperties.getProperty("harmony.env.clusterType", DEFAULT);

        if (cluster.equals(DEFAULT)) {
            try {
                String hostname = InetAddress.getLocalHost().getHostName();
                PropertyProviderFactory propertyProviderFactory = new ZipFilePropertyProviderFactory(configurationFile);
                PropertyProvider provider = propertyProviderFactory.createPropertyProvider();
                HarmonyPropertySource properties = provider.getPropertySource(new HarmonyConfigPath(String.format("%s/%s/%s", application, universe, clusterType)));
                LinkedList<ImmutablePair<String, String>> clusterNamesByHost = new LinkedList<>();
                HashSet<String> clusterNamesByPort = new HashSet<>();
                String processPort = System.getProperty("server.port");
                for (Map.Entry entry : properties.getProperties().entrySet()) {
                    String key = (String) entry.getKey();
                    String val = (String) entry.getValue();
                    if (val.equals(hostname)) {
                        String[] fields = key.split("\\.");
                        if (fields.length > 2) {
                            clusterNamesByHost.add(new ImmutablePair<>(fields[0], fields[1]));
                        }
                    } else if (val.equals(processPort)) {
                        String[] fields = key.split("\\.");
                        if (fields.length > 1) {
                            clusterNamesByPort.add(fields[0]);
                        }
                    }
                }

                for (ImmutablePair<String, String> p : clusterNamesByHost) {
                    if (clusterNamesByPort.contains(p.getKey())) {
                        cluster = p.getKey();
                        node = p.getValue();
                        break;
                    }
                }

                if (cluster.equals(DEFAULT)) {
                    logger.error(String.format("No cluster name found according to hostname: %s and port: %s", hostname, processPort));
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException("Failed to resolve hostname");
            }
        }

        // init environment properties
        harmonyEnvironment.setApplication(application);
        harmonyEnvironment.setUniverse(universe);
        harmonyEnvironment.setClusterType(clusterType);
        harmonyEnvironment.setCluster(cluster);
        harmonyEnvironment.setNode(node);

        // init configuration crypto mode
        cryptoMode = overrideProperties.getProperty("configuration.crypto", "real");
        fallBackEnabled = Boolean.valueOf(overrideProperties.getProperty("configuration.crypto.fallback", "true"));

        // init crypto service
        CryptoService cryptoService = null;
        if (cryptoMode != null && cryptoMode.equals("dummy")) {
            logger.info("Use dummy crypto service");
            cryptoService = new DummyCryptoServiceImpl("_encrypted_");
        } else {
            File keyFile = new File(baseDir, "conf/config.key");
            List<SimpleCipher> ciphers = new ArrayList<>();
            TripleDESCipher desCipher = null;
            if (keyFile.exists()) {
                logger.info("Use Configuration Service with decryption key {}.", keyFile);
                try (
                        FileInputStream fis = new FileInputStream(keyFile.getCanonicalPath());
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis))
                ) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        byte[] keyBytes = line.getBytes(StandardCharsets.UTF_8);
                        if (keyBytes.length == 24) {
                            desCipher = new TripleDESCipher(keyBytes);
                            ciphers.add(desCipher);
                        } else if (keyBytes.length == 32) {
                            ciphers.add(new AESGCMCipher(keyBytes));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.info("{} not found. Use Configuration Service without decryption.", keyFile);
            }
            cryptoService = new CryptoServiceImpl(new FallbackCipher(ciphers, new FindFirstSuccess(), fallBackEnabled, desCipher));
        }

        logger.info("Initializing HarmonyPropertyConfiguration from {} with {}",
                new Object[]{file, harmonyEnvironment.toString()});

        // init property provider factory
        PropertyProviderFactory propertyProviderFactory = null;
        propertyProviderFactory = new ZipFilePropertyProviderFactory(configurationFile);
        propertyProviderFactory = new CachedPropertyProviderFactory(propertyProviderFactory);
        propertyProviderFactory = new FlattedProperyProviderFactory(propertyProviderFactory);
        if (cryptoService != null) {
            propertyProviderFactory = new EncryptedPropertyProviderFactory(propertyProviderFactory, cryptoService);
        }
        propertyProviderFactory = new CachedPropertyProviderFactory(propertyProviderFactory);

        // init configuration service
        staticConfigurationService = new HarmonyConfigurationService(propertyProviderFactory, harmonyEnvironment, overrideProperties);
    }

    @Autowired
    private ZooKeeperConfig zooKeeperConfig;

    @Autowired
    private ZooKeeperService zooKeeperService;

    private ConfigurationService configurationService;

    @Bean
    public ConfigurationService configurationService() {
        if (zooKeeperConfig.isDynamicPropertiesEnabled()) {
            PropertyProviderFactory propertyProviderFactory;
            propertyProviderFactory = new ZooKeeperPropertyProviderFactory(zooKeeperConfig, zooKeeperService);
            propertyProviderFactory = new CachedPropertyProviderFactory(propertyProviderFactory);
            ConfigurationService dynamicConfigurationService = new HarmonyConfigurationService(propertyProviderFactory, harmonyEnvironment, null);
            configurationService = new CompositeConfigurationService(staticConfigurationService, dynamicConfigurationService);
            return configurationService;
        } else {
            return staticConfigurationService;
        }
    }

    @Bean
    public HarmonyEnvironment harmonyEnvironment() {
        return harmonyEnvironment;
    }
}
