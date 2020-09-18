/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;


import com.ea.eadp.harmony.configuration.crypto.cipher.Cipher;
import com.ea.eadp.harmony.configuration.crypto.cipher.CipherAlgorithm;

public interface SimpleCipher extends Cipher {
    CipherAlgorithm getAlgorithm();
}
