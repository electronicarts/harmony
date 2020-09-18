/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class FieldEvaluator {
    public static Pattern REFERENCE_PATTERN = Pattern.compile("<<(\\w+)>>");

    protected Field field;
    protected boolean isFolder;
    protected String fieldPath;
    protected Class fieldType;
    protected Class valueType;
    protected Object returnValue;
    protected boolean isMapEvaluated = false;

    public Object getReturnValue() {
        return returnValue;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public Field getField() {
        return field;
    }

    public FieldEvaluator(Field field, String fieldPath, Class valueType, Boolean isFolder) {
        this.field = field;
        this.fieldPath = fieldPath;
        this.isFolder = isFolder;
        this.fieldType = field.getType();
        this.valueType = valueType;
    }

    public abstract boolean evaluate(Map<String, Object> fieldNameObjectMapping, EntityBuilder builder) throws IllegalAccessException, InstantiationException;
}
