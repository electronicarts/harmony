/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

/**
 * Created by juding on 7/26/16.
 */
public enum EmailCategory {
    INFO, WARN, ERROR, NEVER;

    public static String getAllValues() {
        StringBuilder retValue = new StringBuilder();
        EmailCategory[] allValues = EmailCategory.values();
        for (EmailCategory category : allValues){
            retValue.append(category.name()).append(",");
        }

        return retValue.length() > 0 ? retValue.substring(0, retValue.length()-1):retValue.toString();
    }
}
