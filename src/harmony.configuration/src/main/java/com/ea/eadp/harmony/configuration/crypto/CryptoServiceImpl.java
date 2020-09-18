/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto;

import com.ea.eadp.harmony.configuration.crypto.cipher.Cipher;
import org.apache.commons.codec.binary.Base64;

public class CryptoServiceImpl implements CryptoService {
    private final Cipher cipher;

    public CryptoServiceImpl(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String encryptValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            byte[] encrypted = Base64.encodeBase64(cipher.encrypt(value.getBytes("UTF-8")).get());
            return new String(encrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Unable to encrypt value ***", e);
        }
    }

    @Override
    public String decryptValue(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            byte[] decrypted = cipher.decrypt(Base64.decodeBase64(encryptedValue.getBytes("UTF-8"))).get();
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Unable to decrypt value " + encryptedValue, e);
        }
    }
}
