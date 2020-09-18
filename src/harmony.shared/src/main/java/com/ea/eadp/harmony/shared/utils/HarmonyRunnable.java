/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public abstract class HarmonyRunnable implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(HarmonyRunnable.class);

    public static final String HEALTH_CHECK = "HEALTH_CHECK";

    private String logContext;

    public HarmonyRunnable(String logContext) {
        this.logContext = logContext;
    }

    public static String getLogContext() {
        return MDC.get("logContext");
    }

    @Override
    public void run() {
        if (logContext != null) {
            try {
                MDC.put("logContext", logContext);
                runInternal();
            } catch (Exception e) {
                logger.error("Uncaught error in thread: " + Thread.currentThread().getName(), e);
            } finally {
                MDC.remove("logContext");
            }
        } else {
            try {
                runInternal();
            } catch (Exception e) {
                logger.error("Uncaught error in thread: " + Thread.currentThread().getName(), e);
            }
        }
    }

    protected abstract void runInternal();
}
