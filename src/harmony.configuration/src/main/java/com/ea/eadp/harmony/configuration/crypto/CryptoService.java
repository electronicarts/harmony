/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto;

public interface CryptoService {

    String encryptValue(String value);

    String decryptValue(String encryptedValue);
}
