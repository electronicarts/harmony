/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.ZookeeperORM;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by VincentZhang on 5/9/2018.
 */
@Component
public class FieldEvaluatorBuilder {
    public FieldEvaluator createEvaluator(Field field, String fieldPath, Class valueType, Boolean isFolder){
        if(field.getType() == String.class){
            return new StringFieldEvaluator(field, fieldPath, valueType, isFolder);
        }else if(field.getType() == Map.class){
            return new MapFieldEvalutor(field, fieldPath, valueType, isFolder);
        }
        return null;
    }
}
