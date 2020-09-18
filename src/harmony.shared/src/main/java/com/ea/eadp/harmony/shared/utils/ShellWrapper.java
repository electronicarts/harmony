/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by juding on 10/24/2014.
 */
public class ShellWrapper {
    private final static Logger logger = LoggerFactory.getLogger(ShellWrapper.class);

    public static class ExitRecord {
        public final int retVal;
        public final String outStr;
        public final String errStr;

        public ExitRecord(int retVal, String outStr, String errStr) {
            this.retVal = retVal;
            this.outStr = outStr;
            this.errStr = errStr;
        }

        public boolean success() {
            return (retVal == 0);
        }

        public boolean failure() {
            return (retVal != 0);
        }

        @Override
        public String toString() {
            return "ExitRecord{" +
                    "retVal=" + retVal +
                    ", outStr='" + outStr + '\'' +
                    ", errStr='" + errStr + '\'' +
                    '}';
        }
    }

    private static String readAll(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                if (builder.length() != 0) {
                    builder.append("\n");
                }
                builder.append(line);
                line = reader.readLine();
            }
            return builder.toString();
        } catch (IOException ex) {
            logger.error("Failed to read stream", ex);
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.error("Failed to close stream", ex);
            }
        }
    }

    public static ExitRecord executeCmd(String cmd, Object... args) {
        return executeCmd(true, cmd, args);
    }

    public static ExitRecord executeCmd(boolean sudo, String cmd, Object... args) {
        Runtime r = Runtime.getRuntime();
        try {
            String realCmd = String.format(cmd, args);
            if (sudo)
                realCmd = "sudo " + realCmd;
            logger.info("Executing realCmd: " + realCmd);

            Process p = r.exec(realCmd);
            InputStream stdOut = p.getInputStream();
            InputStream stdErr = p.getErrorStream();
            try {
                int ret = p.waitFor();
                String out = readAll(stdOut);
                String err = readAll(stdErr);
                return new ExitRecord(ret, out, err);
            } finally {
                try {
                    stdOut.close();
                    stdErr.close();
                } catch (IOException ex) {
                    logger.error("Failed to close command streams", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to execute command", ex);
            return null;
        }
    }
}
