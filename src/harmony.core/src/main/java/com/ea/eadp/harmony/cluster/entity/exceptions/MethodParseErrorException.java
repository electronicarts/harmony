/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity.exceptions;

/**
 * Created by VincentZhang on 5/25/2018.
 */
public class MethodParseErrorException extends HarmonyEntityException {
    private String methodName;
    public MethodParseErrorException(String methodName){
        this.methodName = methodName;
    }

    public String getMethodName(){
        return methodName;
    }
}
