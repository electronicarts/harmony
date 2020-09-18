/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import java.lang.annotation.*;

/**
 * Created by leilin on 10/21/2014.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CommandHandler {
    Class<? extends HarmonyCommand> value();
}
