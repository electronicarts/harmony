/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStep;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;

import java.util.Map;

/**
 * Created by VincentZhang on 5/29/2018.
 */
public abstract class MySQLNodeCheckStep extends NodeCheckStep {

    @Override
    public NodeCheckStepResult check(Map dataObjectMap) {
        MySqlServiceConfig config = (MySqlServiceConfig) NodeCheckContext.get("config");
        dataObjectMap.put("config", config);
        return internalCheck(config);
    }

    abstract NodeCheckStepResult internalCheck(MySqlServiceConfig config);
}
