/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

/**
 * Created by VincentZhang on 5/7/2018.
 */
public enum ConsoleColors {
    RESET("\u001b[0m"),
    BLACK("\u001b[30m"),
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    MAGENTA("\u001b[35m"),
    CYAN("\u001b[36m"),
    WHITE("\u001b[37m");

    private final String color;

    ConsoleColors(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return this.color;
    }
}
