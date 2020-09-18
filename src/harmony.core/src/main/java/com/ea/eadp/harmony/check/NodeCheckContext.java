/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

import java.util.HashMap;
import java.util.Map;

public class NodeCheckContext {
    static private ThreadLocal<Map> context = new ThreadLocal();

    public static void put(String key, Object value){
        getMap().put(key, value);
    }

    public static Object get(String key){

        return getMap().get(key);
    }

    private static Map getMap(){
        if(context.get() == null){
            context.set(new HashMap());
        }
        return context.get();
    }

    public static void clear(){
        getMap().clear();
    }
}
