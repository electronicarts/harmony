/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;


import com.ea.eadp.harmony.configuration.crypto.cipher.exception.CipherException;

public class Success extends Result {
    private final CipherAlgorithm algorithm;
    private final byte[] result;
    public Success(CipherAlgorithm algorithm, byte[] result) {
        this.algorithm = algorithm;
        this.result = result;
    }

    @Override
    public byte[] get() throws CipherException {
        return result;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String toString() {
        return "Success{" +
                "algorithm=" + algorithm +
                '}';
    }
}
