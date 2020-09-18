/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.cluster.entity.exceptions.MethodParseErrorException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor {
    private Object targetObject;
    private Method targetMethod;
    public static Pattern PARAMPATTERN = Pattern.compile("<(\\w+)>");

    // In command line, the map from idx->parameter_name
    private Map<Integer, String> cmdIdxNameMap = new HashMap<>();
    private Map<String, Integer> cmdNameIdxMap = new HashMap<>();

    // In Java method, the map from parameter_name->idx
    private Map<String, Integer> methodNameIdxMap = new HashMap<>();

    private String commandHelp = "";

    public CommandExecutor(Object targetObject, Method targetMethod, String[] subPaths) throws MethodParseErrorException {
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;

        parseParameterMapping(subPaths);
    }

    public String getCommandHelp(){
        return commandHelp;
    }

    private void parseParameterMapping(String[] subPaths) throws MethodParseErrorException {
        for (int i = 0; i < subPaths.length; i++) {
            Matcher matcher = PARAMPATTERN.matcher(subPaths[i]);
            if (matcher.find()) {
                cmdIdxNameMap.put(i, matcher.group(1));
                cmdNameIdxMap.put(matcher.group(1), i);
            }
            commandHelp += subPaths[i] + " ";
        }

        Parameter[] params = targetMethod.getParameters();
        for (int i = 0; i < params.length; i++) {
            String fieldName = params[i].getName();

            Integer fieldIdx = cmdNameIdxMap.get(fieldName);
            if (fieldIdx == null) {  // Field defined method but not in annotation
                throw new MethodParseErrorException(targetObject.getClass().getCanonicalName() + "." + targetMethod.getName());
            }

            methodNameIdxMap.put(fieldName, i);
        }

        if (methodNameIdxMap.size() != cmdIdxNameMap.size()) {  // Field defined in annotation but not in method
            throw new MethodParseErrorException(targetObject.getClass().getCanonicalName() + "." + targetMethod.getName());
        }
    }


    public String execute(List<String> words) throws InvocationTargetException, IllegalAccessException {
        ArrayList<String> neededArgs = new ArrayList<>();
        // Add null values
        for (int i = 0; i < methodNameIdxMap.size(); i++) {
            neededArgs.add(null);
        }

        // Execute the command
        for (int i = 0; i < words.size(); i++) {
            String currentWord = words.get(i);
            if (null != cmdIdxNameMap.get(i)) {
                String paramName = cmdIdxNameMap.get(i);
                Integer methodIdx = methodNameIdxMap.get(paramName);
                neededArgs.set(methodIdx, currentWord);
            }
        }

        for (int i = 0; i < methodNameIdxMap.size(); i++) {
            if (neededArgs.get(i) == null) {
                return "Command Error, format should be:" + commandHelp;
            }
        }

        return (String) targetMethod.invoke(targetObject, neededArgs.toArray());
    }
}
