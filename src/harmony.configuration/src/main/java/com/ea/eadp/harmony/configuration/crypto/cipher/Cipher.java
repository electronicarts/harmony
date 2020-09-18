/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;

public interface Cipher {
    Result encrypt(byte[] data);

    Result decrypt(byte[] data);
}
