/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.shared.event.ApplicationStartedEvent;
import com.ea.eadp.harmony.shared.event.EventHandler;
import com.ea.eadp.harmony.shared.event.EventHandlerClass;
import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;

/**
 * User: leilin
 * Date: 10/2/14
 */
@EventHandlerClass
public interface LeaderElectionService {
    @EventHandler(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent e);

    @EventHandler(ApplicationClosedEvent.class)
    public void onApplicationClosed(ApplicationClosedEvent e);
}
