/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by VincentZhang on 5/9/2018.
 */
public class StringFieldEvaluator extends FieldEvaluator{

    public StringFieldEvaluator(Field field, String fieldPath, Class valueType, Boolean isFolder) {
        super(field, fieldPath, valueType, isFolder);
    }

    private boolean internal_evaluate(EntityBuilder builder) throws IllegalAccessException, InstantiationException {
        List<String> allPendingFields = new ArrayList();
        Matcher match = REFERENCE_PATTERN.matcher(this.fieldPath);
        if (match.find()) {
            for (int i = 0; i < match.groupCount(); i++) {
                allPendingFields.add(match.group(i));
            }
        }

        if (allPendingFields.size() == 0) {
            // Perform the real evaluation
            this.returnValue = builder.deepEvaluateValue(fieldPath, isFolder, field);
            return true;
        }

        return false;
    }

    @Override
    public boolean evaluate(Map<String, Object> fieldNameObjectMapping, EntityBuilder builder) throws IllegalAccessException, InstantiationException {
        for (Map.Entry<String, Object> fieldNameObjectEntry : fieldNameObjectMapping.entrySet()) {
            String fieldName = fieldNameObjectEntry.getKey();
            if (!(fieldNameObjectEntry.getValue() instanceof FieldEvaluator)) {
                String fieldValue = "Unknown Node";
                if(null != fieldNameObjectEntry.getValue() ){
                    fieldValue = fieldNameObjectEntry.getValue().toString();
                }

                fieldPath = fieldPath.replaceAll("<<" + fieldName + ">>", fieldValue);
            }
        }

        if (internal_evaluate(builder))
            return true;

        return false;
    }
}
