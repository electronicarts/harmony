/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * User: leilin
 * Date: 10/6/14
 */
public class HarmonyUtils {
    public static String exceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    public static List<String> splitAndTrim(String str, String seperator) {
        ArrayList<String> ret = new ArrayList<String>();
        ;
        if (str != null && !str.isEmpty()) {
            for (String part : str.split(seperator)) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    ret.add(trimmed);
                }
            }
        }

        return ret;
    }

    public static String getDefaultClusterId() {
        try {
            String fullHost = InetAddress.getLocalHost().getHostName();
            String clusterId = fullHost.split("\\.")[0];
            return clusterId;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logProperties(Logger logger, Properties properties, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            if (properties.isEmpty()) {
                return;
            }
            logger.info("=================================================");
            logger.info(message, args);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                logger.info("{}={}", entry.getKey(), entry.getValue());
            }
            logger.info("=================================================");
        }
    }
}
