/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;

import com.ea.eadp.harmony.configuration.crypto.cipher.CipherAlgorithm;
import com.ea.eadp.harmony.configuration.crypto.cipher.Failure;
import com.ea.eadp.harmony.configuration.crypto.cipher.Result;
import com.ea.eadp.harmony.configuration.crypto.cipher.Success;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESGCMCipher implements SimpleCipher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESGCMCipher.class);
    private static final int NONCE_SIZE = 12;
    private static final int KEY_SIZE = 256;
    private static final String ALGO = "AES";
    private static final String MODE = "GCM";
    private static final String PADDING = "PKCS5Padding";
    private static final int BLOCK_SIZE = 128;
    private static final SecureRandom rng = new SecureRandom();

    private static final String TRANSFORMATION = String.format("%s/%s/%s", ALGO, MODE, PADDING);

    private SecretKey key;

    public AESGCMCipher(byte[] keyBytes) {
        if (keyBytes == null) {
            throw new IllegalArgumentException("keyBytes is null");
        }
        if ((keyBytes.length * 8) != KEY_SIZE) {
            throw new IllegalArgumentException(String.format("invalid key size, should be %d rather than %d", KEY_SIZE, keyBytes.length * 8));
        }
        key = new SecretKeySpec(keyBytes, ALGO);
    }

    @Override
    public CipherAlgorithm getAlgorithm() {
        return CipherAlgorithm.AES;
    }

    @Override
    public Result encrypt(byte[] data) {
        if (data == null) {
            LOGGER.warn("input data byte array is null");
            return new Failure(data, new IllegalArgumentException("input data byte array is null"));
        }
        byte[] nonce = new byte[NONCE_SIZE];
        rng.nextBytes(nonce);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(BLOCK_SIZE, nonce);
        try {
            javax.crypto.Cipher encrypt = javax.crypto.Cipher.getInstance(TRANSFORMATION);
            encrypt.init(javax.crypto.Cipher.ENCRYPT_MODE, key, parameterSpec);
            int len = encrypt.getOutputSize(data.length);
            byte[] result = new byte[len + NONCE_SIZE];
            System.arraycopy(nonce, 0, result, 0, NONCE_SIZE);
            encrypt.doFinal(data, 0, data.length, result, NONCE_SIZE);
            return new Success(this.getAlgorithm(), result);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.warn("failed to encrypt", e);
            return new Failure(data, e);
        }
    }

    @Override
    public Result decrypt(byte[] data) {
        if (data == null) {
            LOGGER.warn("input data byte array is null");
            return new Failure(data, new IllegalArgumentException("input data byte array is null"));
        }
        if (data.length <= NONCE_SIZE) {
            LOGGER.warn(String.format("data length %d is not greater than nonce length %d", data.length, NONCE_SIZE));
            return new Failure(data, new IllegalArgumentException(String.format("data length %d is not greater than nonce length %d", data.length, NONCE_SIZE)));
        }
        byte[] nonce = Arrays.copyOfRange(data, 0, NONCE_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(data, NONCE_SIZE, data.length);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(BLOCK_SIZE, nonce);
        try {
            javax.crypto.Cipher decrypt = javax.crypto.Cipher.getInstance(TRANSFORMATION);
            decrypt.init(javax.crypto.Cipher.DECRYPT_MODE, key, parameterSpec);
            byte[] result = decrypt.doFinal(ciphertext);
            return new Success(this.getAlgorithm(), result);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.warn("failed to decrypt",e);
            return new Failure(data, e);
        }
    }
}

