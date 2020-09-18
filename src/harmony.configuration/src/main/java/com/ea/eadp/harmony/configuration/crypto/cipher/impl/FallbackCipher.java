/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;


import com.ea.eadp.harmony.configuration.crypto.cipher.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FallbackCipher implements Cipher {
    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackCipher.class);
    private List<SimpleCipher> ciphers;
    private CipherResultAggregate aggregator;
    private Boolean enabled;
    private SimpleCipher defaultCipher;

    public FallbackCipher(List<SimpleCipher> ciphers, CipherResultAggregate aggregate, Boolean enabled, SimpleCipher defaultCipher) {
        if (ciphers == null || ciphers.isEmpty()) {
            throw new IllegalArgumentException("null ciphers");
        }
        if (aggregate == null) {
            throw new IllegalArgumentException("null aggregate");
        }
        this.ciphers = ciphers;
        this.aggregator = aggregate;
        this.enabled = Optional.ofNullable(enabled).orElse(false);
        this.defaultCipher = defaultCipher;
    }

    @Override
    public Result encrypt(byte[] data) {
        if(data == null) {
            return new Failure(data, new IllegalArgumentException("null input buffer"));
        }
        if(enabled) {
            return aggregator.aggregate(ciphers, data, CipherMode.ENCRYPT);
        } else {
            if(defaultCipher == null) {
                LOGGER.warn("default cipher is null, returning null");
                return new Success(null, null);
            } else {
                return defaultCipher.encrypt(data);
            }
        }
    }

    @Override
    public Result decrypt(byte[] data) {
        if(data == null) {
            return new Failure(data, new IllegalArgumentException("null input buffer"));
        }
        if(enabled) {
            return aggregator.aggregate(ciphers, data, CipherMode.DECRYPT);
        } else {
            if(defaultCipher == null) {
                LOGGER.warn("default cipher is null, returning null");
                return new Success(null, null);
            } else {
                return defaultCipher.decrypt(data);
            }
        }
    }

    public List<SimpleCipher> getCiphers() {
        return ciphers;
    }

    public void setCiphers(List<SimpleCipher> ciphers) {
        this.ciphers = ciphers;
    }

    public CipherResultAggregate getAggregator() {
        return aggregator;
    }

    public void setAggregator(CipherResultAggregate aggregator) {
        this.aggregator = aggregator;
    }
}
