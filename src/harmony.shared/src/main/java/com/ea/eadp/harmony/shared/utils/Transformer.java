/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

/**
 * Created by juding on 10/27/2014.
 */
public interface Transformer<TIn, TOut, TThr extends Throwable> {
    TOut execute(TIn input) throws TThr;
}
