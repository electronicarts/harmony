/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.configuration.crypto.cipher;

import org.junit.Assert;
import org.junit.Test;

public class SuccessTest {
    @Test
    public void testToString() throws Exception {
        Result result = new Success(null, null);
        String s = result.toString();
        Assert.assertNotNull(s);
        Assert.assertEquals("Success{algorithm=null}", s);
    }

}