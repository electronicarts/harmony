/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;

import com.ea.eadp.harmony.configuration.crypto.cipher.Cipher;
import com.ea.eadp.harmony.configuration.crypto.cipher.CipherAlgorithm;
import com.ea.eadp.harmony.configuration.crypto.cipher.Result;

import java.util.Map;

public class SwitchableCipher implements Cipher {
    private SimpleCipher defaultCipher;
    private Boolean enabled;
    private Map<CipherAlgorithm, SimpleCipher> algorithmSimpleCipherMap;
    private CipherAlgorithm algorithm;

    public SwitchableCipher(SimpleCipher defaultCipher, Boolean enabled, Map<CipherAlgorithm, SimpleCipher> algorithmSimpleCipherMap, CipherAlgorithm algorithm) {
        this.defaultCipher = defaultCipher;
        this.enabled = enabled;
        this.algorithmSimpleCipherMap = algorithmSimpleCipherMap;
        this.algorithm = algorithm;
    }

    public SimpleCipher getDefaultCipher() {
        return defaultCipher;
    }

    public void setDefaultCipher(SimpleCipher defaultCipher) {
        this.defaultCipher = defaultCipher;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<CipherAlgorithm, SimpleCipher> getAlgorithmSimpleCipherMap() {
        return algorithmSimpleCipherMap;
    }

    public void setAlgorithmSimpleCipherMap(Map<CipherAlgorithm, SimpleCipher> algorithmSimpleCipherMap) {
        this.algorithmSimpleCipherMap = algorithmSimpleCipherMap;
    }

    public CipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(CipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public Result encrypt(byte[] data) {
        return getCipher().encrypt(data);
    }

    public SimpleCipher getCipher() {
        if (Boolean.FALSE.equals(enabled)) {
            return defaultCipher;
        } else {
            SimpleCipher simpleCipher = algorithmSimpleCipherMap.get(algorithm);
            if(simpleCipher == null) {
                throw new IllegalStateException(String.format("cannot find cipher %s", algorithm.getValue()));
            } else {
                return simpleCipher;
            }
        }
    }

    @Override
    public Result decrypt(byte[] data) {
        return getCipher().decrypt(data);
    }
}
