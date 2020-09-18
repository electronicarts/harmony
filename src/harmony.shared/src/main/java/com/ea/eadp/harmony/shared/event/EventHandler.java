/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import java.lang.annotation.*;

/**
 * User: leilin
 * Date: 10/8/14
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EventHandler {
    Class<? extends HarmonyEvent> value();
}
