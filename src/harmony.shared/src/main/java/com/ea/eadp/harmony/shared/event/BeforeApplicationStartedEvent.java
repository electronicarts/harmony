/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import org.springframework.context.ApplicationContext;

/**
 * Created by leilin on 10/16/2014.
 */
public class BeforeApplicationStartedEvent extends HarmonyEvent {
    private ApplicationContext context;

    public BeforeApplicationStartedEvent() {
    }

    public BeforeApplicationStartedEvent(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
