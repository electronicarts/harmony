/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFieldEvalutor extends FieldEvaluator {
    public MapFieldEvalutor(Field field, String fieldPath, Class valueType, Boolean isFolder) {
        super(field, fieldPath, valueType, isFolder);
    }

    public boolean evaluate(Map<String, Object> fieldNameObjectMapping, EntityBuilder builder) throws IllegalAccessException, InstantiationException {
        for (Map.Entry<String, Object> fieldNameObjectEntry : fieldNameObjectMapping.entrySet()) {
            String fieldName = fieldNameObjectEntry.getKey();
            if (!(fieldNameObjectEntry.getValue() instanceof FieldEvaluator)) {
                if (fieldNameObjectEntry.getValue() instanceof List) {
                    List<String> keys = (List) fieldNameObjectEntry.getValue();
                    Map returnMap = new HashMap();
                    for (String key : keys) {
                        String newFieldPath = fieldPath.replaceAll("<<" + fieldName + ">>", key);

                        returnMap.put(key,builder.createMappedObject(this.valueType,
                                newFieldPath));
                    }
                    returnValue = returnMap;
                    return true;
                }
            }
        }
        return false;
    }
}

