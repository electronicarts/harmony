/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import org.springframework.context.ApplicationContext;

/**
 * User: leilin
 * Date: 10/8/14
 */
public class ApplicationStartedEvent extends HarmonyEvent {
    private ApplicationContext context;

    public ApplicationStartedEvent() {
    }

    public ApplicationStartedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
