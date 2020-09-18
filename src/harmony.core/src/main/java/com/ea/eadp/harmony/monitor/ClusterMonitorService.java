/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.event.ClusterCheckEvent;
import com.ea.eadp.harmony.event.EmailCategoryChangedEvent;
import com.ea.eadp.harmony.event.ServiceMasterChangedEvent;
import com.ea.eadp.harmony.shared.event.EventHandler;
import com.ea.eadp.harmony.shared.event.EventHandlerClass;
import com.ea.eadp.harmony.event.MonitorLeaderChangedEvent;

/**
 * User: leilin
 * Date: 10/2/14
 */
@EventHandlerClass
public interface ClusterMonitorService {
    @EventHandler(ClusterCheckEvent.class)
    void onClusterCheck(ClusterCheckEvent event);

    @EventHandler(MonitorLeaderChangedEvent.class)
    void onMonitorLeaderChanged(MonitorLeaderChangedEvent event);

    @EventHandler(ServiceMasterChangedEvent.class)
    void onServiceMasterChanged(ServiceMasterChangedEvent event);

    @EventHandler(EmailCategoryChangedEvent.class)
    void onEmailCategoryChanged(EmailCategoryChangedEvent event);
}
