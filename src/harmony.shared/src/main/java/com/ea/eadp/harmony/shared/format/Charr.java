/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.format;

class Charr {

    protected static final char S = ' ';

    protected static final char NL = '\n';

    protected static final char P = '+';

    protected static final char D = '-';

    protected static final char VL = '|';

    private final int x;

    private final int y;

    private final char c;

    protected Charr(int x, int y, char c) {
        this.x = x;
        this.y = y;
        this.c = c;
    }

    protected int getX() {
        return x;
    }

    protected int getY() {
        return y;
    }

    protected char getC() {
        return c;
    }

}