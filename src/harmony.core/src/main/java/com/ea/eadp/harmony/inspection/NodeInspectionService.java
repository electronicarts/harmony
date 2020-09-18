/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.inspection;

import com.ea.eadp.harmony.shared.event.ApplicationClosedEvent;
import com.ea.eadp.harmony.shared.event.EventHandler;
import com.ea.eadp.harmony.shared.event.EventHandlerClass;
import com.ea.eadp.harmony.event.NodeInspectionEvent;

/**
 * User: leilin
 * Date: 10/2/14
 */
@EventHandlerClass
public interface NodeInspectionService {
    @EventHandler(NodeInspectionEvent.class)
    void onNodeInspection(NodeInspectionEvent event);

    @EventHandler(ApplicationClosedEvent.class)
    void onApplicationClosed(ApplicationClosedEvent e);
}
