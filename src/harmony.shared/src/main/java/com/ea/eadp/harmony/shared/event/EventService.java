/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

/**
 * User: leilin
 * Date: 10/8/14
 */
public interface EventService {
    void raiseEvent(HarmonyEvent event);

    void raiseEventAsync(HarmonyEvent event);

    void registerHandler(Object handlerInstance);

    void unregisterHandler(Object handlerInstance);

    void close();
}
