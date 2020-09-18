/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.warmup;

import com.ea.eadp.harmony.config.ServiceConfig;

public interface NodeWarmUpExecutor<TConfig extends ServiceConfig> {
    void warmUpNode(TConfig config);

    void killWarmUp(TConfig config);
}
