/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.inspection;

import com.ea.eadp.harmony.config.ServiceConfig;

/**
 * Created by leilin on 10/16/2014.
 */
public interface NodeInspector<TConfig extends ServiceConfig> {
    InspectionResult inspectNode(TConfig config);
}
