/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.provider;

import com.ea.eadp.harmony.configuration.crypto.CryptoService;
import com.ea.eadp.harmony.configuration.properties.HarmonyConfigPath;
import com.ea.eadp.harmony.configuration.properties.HarmonyPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

/**
 * Created by leilin on 10/17/2014.
 */
public class EncryptedPropertyProviderFactory extends PropertyProviderWrapperFactory {

    public static final String ENCRYPTED_SUFFIX = ".encrypted";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CryptoService cryptoService;

    public EncryptedPropertyProviderFactory(PropertyProviderFactory factory, CryptoService cryptoService) {
        super(factory);
        this.cryptoService = cryptoService;
    }

    @Override
    protected HarmonyPropertySource getPropertySource(HarmonyConfigPath path, PropertyProvider provider) {
        HarmonyPropertySource source = provider.getPropertySource(path);
        Properties decryptedProperties = decryptProperties(source.getProperties());
        return new HarmonyPropertySource(source.getPropertySourceType(), source.getPath(), decryptedProperties);
    }

    private Properties decryptProperties(Properties properties) {
        if (cryptoService == null) {
            throw new IllegalStateException("cryptoService is null");
        }

        Properties decrypted = new Properties();

        for (Map.Entry<Object, Object> entry : new HashSet<Map.Entry<Object, Object>>(properties.entrySet())) {

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (key.endsWith(ENCRYPTED_SUFFIX) && !"decrypted".equals(value)) {

                String newKey = key.substring(0, key.length() - ENCRYPTED_SUFFIX.length());

                if (properties.containsKey(newKey)) {
                    logger.warn("Decrypted property " + newKey + " already exists; it will be replaced.");
                }

                String newValue = cryptoService.decryptValue(value);

                decrypted.put(newKey, newValue);
                properties.put(key, "***decrypted***");

                logger.info("Decrypted property " + key);
            } else {
                if (!decrypted.containsKey(key)) {
                    decrypted.put(key, value);
                } else {
                    logger.info("property {} already exist, skipping", key);
                }
            }
        }

        return decrypted;
    }
}
