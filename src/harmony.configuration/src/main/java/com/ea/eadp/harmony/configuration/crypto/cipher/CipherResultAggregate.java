/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;


import com.ea.eadp.harmony.configuration.crypto.cipher.impl.SimpleCipher;

import java.util.List;

public interface CipherResultAggregate {
    Result aggregate(List<SimpleCipher> ciphers, byte[] data, CipherMode encrypt);
}
