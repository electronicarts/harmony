/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

import com.ea.eadp.harmony.config.ServiceConfig;

/**
 * Created by juding on 11/7/14.
 */
public interface NodeChecker<TConfig extends ServiceConfig> {
    CheckResult checkNode(TConfig config);
}
