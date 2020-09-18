/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by leilin on 10/21/2014.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class HarmonyCommandResult {
    private ResultType resultType = ResultType.SUCCEEDED;
    private String resultMessage;
    private String errorMessage;

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "HarmonyCommandResult{" +
                "resultType=" + resultType +
                ", resultMessage='" + resultMessage + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
