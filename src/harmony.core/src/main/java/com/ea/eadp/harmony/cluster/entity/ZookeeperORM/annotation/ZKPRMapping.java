/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Make this annotation accessible at runtime via reflection.
@Target({ElementType.FIELD, ElementType.METHOD})       // This annotation can only be applied to class methods.
public @interface ZKPRMapping {
    String path() default ""; // Manually specify the path of this node
    boolean isFolder() default false; // If this is folder, will get the folder name as the property
    Class valueType() default Void.class; // Used only for Map type, because of type erasure, can get in run time.
}
