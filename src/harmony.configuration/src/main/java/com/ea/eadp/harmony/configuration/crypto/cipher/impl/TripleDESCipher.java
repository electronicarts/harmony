/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;

import com.ea.eadp.harmony.configuration.crypto.cipher.CipherAlgorithm;
import com.ea.eadp.harmony.configuration.crypto.cipher.Failure;
import com.ea.eadp.harmony.configuration.crypto.cipher.Result;
import com.ea.eadp.harmony.configuration.crypto.cipher.Success;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TripleDESCipher implements SimpleCipher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripleDESCipher.class);

    private SecretKey key;

    public TripleDESCipher(byte[] keyBytes) {
        key = new SecretKeySpec(keyBytes, getAlgorithm().getValue());
    }

    @Override
    public CipherAlgorithm getAlgorithm() {
        return CipherAlgorithm.TRIPLE_DES;
    }

    @Override
    public Result encrypt(byte[] data) {
        if (data == null) {
            LOGGER.warn("null input buffer");
            return new Failure(data, new IllegalArgumentException("null input buffer"));
        }
        try {
            javax.crypto.Cipher encrypt = javax.crypto.Cipher.getInstance("TripleDES");
            encrypt.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
            return new Success(this.getAlgorithm(), encrypt.doFinal(data));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.warn("failed to encrypt", e);
            return new Failure(data, e);
        }
    }

    @Override
    public Result decrypt(byte[] data) {
        if (data == null) {
            LOGGER.warn("null input buffer");
            return new Failure(data, new IllegalArgumentException("null input buffer"));
        }
        try {
            javax.crypto.Cipher decrypt = javax.crypto.Cipher.getInstance("TripleDES");
            decrypt.init(javax.crypto.Cipher.DECRYPT_MODE, key);
            return new Success(this.getAlgorithm(), decrypt.doFinal(data));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.warn("failed to decrypt", e);
            return new Failure(data, e);
        }
    }


}
