/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto;

/**
 * Created by leilin on 10/14/2014.
 */
public class DummyCryptoServiceImpl implements CryptoService {
    private String dummyPrefix;

    public DummyCryptoServiceImpl(String dummyPrefix) {
        this.dummyPrefix = dummyPrefix;
    }

    @Override
    public String encryptValue(String value) {
        return dummyPrefix + value;
    }

    @Override
    public String decryptValue(String encryptedValue) {
        if (!encryptedValue.startsWith(dummyPrefix)) {
            throw new RuntimeException("Failed to decrypt value:" + encryptedValue);
        }

        return encryptedValue.substring(dummyPrefix.length());
    }
}
