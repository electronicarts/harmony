/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config.annotation;

import java.lang.annotation.*;

/**
 * Created by leilin on 10/15/2014.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceProperty {
    String value() default "";
}
