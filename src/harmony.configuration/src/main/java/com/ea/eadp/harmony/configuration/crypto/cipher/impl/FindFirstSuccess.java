/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher.impl;

import com.ea.eadp.harmony.configuration.crypto.cipher.CipherMode;
import com.ea.eadp.harmony.configuration.crypto.cipher.CipherResultAggregate;
import com.ea.eadp.harmony.configuration.crypto.cipher.Failure;
import com.ea.eadp.harmony.configuration.crypto.cipher.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;


public class FindFirstSuccess implements CipherResultAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindFirstSuccess.class);

    private BiFunction<Result, SimpleCipher, Result> encryptAccumulator = (result, simpleCipher) -> {
        if (result.isSuccess()) {
            return result;
        } else {
            Failure failure = (Failure) result;
            LOGGER.info(String.format("try encrypt using %s", simpleCipher.getAlgorithm()));
            return simpleCipher.encrypt(failure.getInput());
        }
    };

    private BiFunction<Result, SimpleCipher, Result> decryptAccumulator = (result, simpleCipher) -> {
        if (result.isSuccess()) {
            return result;
        } else {
            Failure failure = (Failure) result;
            LOGGER.info(String.format("try encrypt using %s", simpleCipher.getAlgorithm()));
            return simpleCipher.decrypt(failure.getInput());
        }
    };

    private BinaryOperator<Result> resultCombiner = (result, result2) -> {
        if(result == null && result2 == null) {
            throw new IllegalStateException("both result are null");
        }
        if(result == null) {
            return result2;
        }
        if(result2 == null) {
            return result;
        }

        if (result.isSuccess()) {
            return result;
        } else if (result2.isSuccess()) {
            return result2;
        } else {
            return result;
        }
    };

    @Override
    public Result aggregate(List<SimpleCipher> ciphers, byte[] data, CipherMode mode) {
        if(CipherMode.ENCRYPT.equals(mode)) {
            return ciphers.stream().reduce(new Failure(data), encryptAccumulator, resultCombiner);
        } else if(CipherMode.DECRYPT.equals(mode)) {
            return ciphers.stream().reduce(new Failure(data), decryptAccumulator, resultCombiner);
        } else {
            return new Failure(data, "invalid mode");
        }
    }

    BinaryOperator<Result> getResultCombiner() {
        return resultCombiner;
    }

    public BiFunction<Result, SimpleCipher, Result> getEncryptAccumulator() {
        return encryptAccumulator;
    }

    public BiFunction<Result, SimpleCipher, Result> getDecryptAccumulator() {
        return decryptAccumulator;
    }
}
