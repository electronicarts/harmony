/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by juding on 7/28/16.
 */
public class EmailUtils {
    public static String prettyTrunkPath(String path) {
        String[] itemArr = path.split("/");
        return prettyTrunk(itemArr);
    }

    public static String prettyLeaderPathChildren(List<String> children) {
        List<String> ids = new ArrayList<String>(children.size());
        for (String child : children) {
            int idx = child.lastIndexOf("-");
            ids.add(child.substring(idx+1));
        }
        Collections.sort(ids);
        String retStr = "Harmony ID:";
        for (String id : ids)
            retStr += " " + id;
        return retStr + "\n";
    }

    public static String prettyServicePath(String servicePath) {
        String[] itemArr = servicePath.split("/");
        Assert.isTrue(itemArr.length >= 7);
        return prettyTrunk(itemArr)
                + "Service Name:" + itemArr[6] + "\n";
    }

    public static String prettyStatusPath(String path) {
        String[] itemArr = path.split("/");
        return prettyTrunk(itemArr)
                + "Service Name: " + itemArr[6] + "\n"
                + "Node Name: " + itemArr[8] + "\n";
    }

    public static String prettyEmailCategory(EmailCategory emailCategory) {
        String retStr = "Action Item: ";
        switch (emailCategory) {
            case INFO:
                return retStr + "None\n";
            default:
                return retStr + "Fix the issue.\n";
        }
    }

    private static String prettyTrunk(String[] itemArr) {
        return "Server Type: " + itemArr[2] + "\n"
                + "Environment: " + itemArr[3] + "\n"
                + "ClusterInfo Type: " + itemArr[4] + "\n"
                + "ClusterInfo Name: " + itemArr[5] + "\n";
    }
}
