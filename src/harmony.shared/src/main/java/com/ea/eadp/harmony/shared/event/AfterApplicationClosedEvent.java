/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import org.springframework.context.ApplicationContext;

public class AfterApplicationClosedEvent extends HarmonyEvent {
    private ApplicationContext context;

    public AfterApplicationClosedEvent() {
    }

    public AfterApplicationClosedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
