/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM;


import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by VincentZhang on 3/5/2018.
 */
@Component
public class EntityBuilder {
    @Autowired
    private ZooKeeperService zkprService;

    @Autowired
    private FieldEvaluatorBuilder fieldEvaluatorBuilder;

    private void setField(Object object, Field field, Object value, Map<String, Object> fieldNameObjectMapping)
            throws IllegalAccessException, InstantiationException {
        Assert.notNull(zkprService);
        fieldNameObjectMapping.put(field.getName(), value);
        if (!(value instanceof FieldEvaluator)) {
            field.set(object, value);
        }

        boolean someThingChanged = true;
        while (someThingChanged) {
            someThingChanged = false;
            for (Map.Entry<String, Object> fieldEntry : fieldNameObjectMapping.entrySet()) {
                String fieldName = fieldEntry.getKey();
                Object fieldValue = fieldEntry.getValue();

                if (fieldValue instanceof FieldEvaluator) {
                    FieldEvaluator evaluator = (FieldEvaluator) fieldValue;
                    if (evaluator.evaluate(fieldNameObjectMapping, this)) {
                        someThingChanged = true;
                        evaluator.getField().set(object, evaluator.getReturnValue());
                        fieldNameObjectMapping.put(fieldName, evaluator.getReturnValue());
                    }
                }
            }
        }
    }

    // Get a value from zkpr given the type and path.
    // Wrap ugly things into one function.
    public Object deepEvaluateValue(String path, boolean isFolder, Field field)
            throws IllegalAccessException, InstantiationException {
        return deepEvaluateValue(path, isFolder, field.getType(), field.getAnnotatedType().getType());
    }

    public Object deepEvaluateValue(String path, boolean isFolder, Class fieldTypeClz, Type fieldValueType)
            throws InstantiationException, IllegalAccessException {
        Assert.notNull(zkprService);
        Object returnValue = null;
        Class fieldType = fieldTypeClz;

        if (!isFolder) {
            if (fieldType == String.class) { 
                try {
                    returnValue = zkprService.getNodeStringData(path);
                } catch (RuntimeException e) {
                    returnValue = "Can not be evaluated!";
                }
            } else if (fieldType == Map.class) {
                // Get the value type
                Class argumentType = (Class) ((ParameterizedType) fieldValueType).getActualTypeArguments()[1];

                Map returnMap = new HashMap();
                List<String> paths = zkprService.getChildren(path);

                for (String elementPath : paths) {
                    String newZKPRPath = path + "/" + elementPath;
                    Object newObj = createMappedObject(argumentType, newZKPRPath);
                    returnMap.put(elementPath, newObj);
                }

                returnValue = returnMap;
            } else if (fieldType == List.class) {
                returnValue = zkprService.getChildren(path);
            } else {
                // to eliminate the possibility of stack overflow.
                returnValue = createMappedObject(fieldType, path);
            }
        } else { // It's a folder, ensure it has only one child.
            List<String> paths = zkprService.getChildren(path);
            if (paths.size() == 0) {
                return null;
            }

            if (paths.size() != 1) {
                throw new InstantiationException("Path field can only has 1 sub folder");
            }
            returnValue = paths.get(0);
        }
        return returnValue;
    }

    private Object initInstance(Class clz) throws IllegalAccessException, InstantiationException {
        if (clz.equals(Long.class) || clz.equals(long.class)) {
            return new Long(-1);
        }

        return clz.newInstance();
    }

    public Object createMappedObject(Class clz, String zkprPath)
            throws IllegalAccessException, InstantiationException {
        Assert.notNull(zkprService);

        Object returnObject = initInstance(clz);

        //Save the fieldName -> Object mapping in case we need to refer it in other fields by {{ fieldName }}
        Map<String, Object> fieldNameObjectMapping = new HashMap();

        for (Field field : clz.getFields()) {
            String fieldName = field.getName();

            ZKPRMapping mappingAnnotation = field.getAnnotation(ZKPRMapping.class);
            Object fieldValue = null;
            if (mappingAnnotation == null) { // If doesn't have annotation, use default class hierarchy.
                String fieldPath = zkprPath + "/" + fieldName;
                fieldValue = deepEvaluateValue(fieldPath, false, field);
            } else { // Use what is configured in the path of mappingAnnotation
                String fieldPath = zkprPath;
                if (mappingAnnotation.path() != null && !mappingAnnotation.path().isEmpty()) {
                    fieldPath = fieldPath + "/" + mappingAnnotation.path();
                }

                if (FieldEvaluator.REFERENCE_PATTERN.matcher(fieldPath).find()) { // Contain other fields.

                    fieldValue = fieldEvaluatorBuilder.createEvaluator(field, fieldPath, mappingAnnotation.valueType(),
                            mappingAnnotation.isFolder());
                } else {
                    fieldValue = deepEvaluateValue(fieldPath,
                            mappingAnnotation.isFolder(), field);
                }
            }

            setField(returnObject, field, fieldValue, fieldNameObjectMapping);
        }

        return returnObject;
    }
}
