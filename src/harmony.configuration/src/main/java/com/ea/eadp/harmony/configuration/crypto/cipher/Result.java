/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;


import com.ea.eadp.harmony.configuration.crypto.cipher.exception.CipherException;

public abstract class Result {
    public abstract byte[] get() throws CipherException;

    public abstract boolean isSuccess();

    public boolean isFailure() {
        return !isSuccess();
    }
}
