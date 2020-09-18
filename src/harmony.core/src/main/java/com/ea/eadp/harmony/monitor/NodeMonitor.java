/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.config.ServiceConfig;

/**
 * Created by juding on 10/23/2014.
 */
public interface NodeMonitor<TConfig extends ServiceConfig> {
    MonitorResult monitorNode(TConfig config);
}
