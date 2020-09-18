/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;


import com.ea.eadp.harmony.configuration.crypto.cipher.exception.CipherException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Failure extends Result {

    private final CipherException error;
    private byte[] input = null;

    public Failure(byte[] input) {
        if (input != null) {
            this.input = (byte[]) input.clone();
        }
        error = new CipherException();
    }

    public Failure(byte[] input, String errorMsg) {
        if (input != null) {
            this.input = (byte[]) input.clone();
        }
        this.error = new CipherException(errorMsg);
    }

    public Failure(byte[] input, Exception error) {
        if (input != null) {
            this.input = (byte[]) input.clone();
        }
        this.error = new CipherException(error);
    }

    @Override
    public byte[] get() throws CipherException {
        throw error;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    public byte[] getInput() {
        return input;
    }

    @Override
    public String toString() {
        String errorMsg;
        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(byteArrayOutputStream)
        ) {
            error.printStackTrace(ps);
            errorMsg = byteArrayOutputStream.toString();
        } catch (IOException e) {
            errorMsg = "";
        }
        return "Failure{" +
                "error=" + errorMsg +
                '}';
    }
}
