/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

/**
 * Created by juding on 2/24/16.
 */
public interface EmailService {
    void postEmail(final EmailCategory mailCtg, final String mailSbj, final String mailTxt);
    void close();

    boolean isTerminated();
}
