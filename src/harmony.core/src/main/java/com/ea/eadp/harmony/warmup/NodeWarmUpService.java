/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.warmup;

import com.ea.eadp.harmony.event.NodeWarmUpEvent;
import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;
import com.ea.eadp.harmony.shared.event.ApplicationStartedEvent;
import com.ea.eadp.harmony.shared.event.EventHandler;
import com.ea.eadp.harmony.shared.event.EventHandlerClass;

@EventHandlerClass
public interface NodeWarmUpService {
    @EventHandler(NodeWarmUpEvent.class)
    public void onNodeWarmUp(NodeWarmUpEvent event);

    @EventHandler(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent e);

    @EventHandler(ApplicationClosedEvent.class)
    public void onApplicationClosed(ApplicationClosedEvent e);
}
