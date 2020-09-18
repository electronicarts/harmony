/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

/**
 * User: leilin
 * Date: 10/7/14
 */
public interface CommandNoReturn<TIn> {
    void execute(TIn client);
}
