/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;

public enum CipherAlgorithm {
    AES("AES"),
    TRIPLE_DES("TripleDES");

    private String value;

    CipherAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}