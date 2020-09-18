/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import org.springframework.context.ApplicationContext;

/**
 * Created by juding on 10/14/2014.
 */
public class ApplicationClosedEvent extends HarmonyEvent {
    private ApplicationContext context;

    public ApplicationClosedEvent() {
    }

    public ApplicationClosedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
