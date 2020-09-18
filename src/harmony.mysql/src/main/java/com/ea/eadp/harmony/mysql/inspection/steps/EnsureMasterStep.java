/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 5/28/2018.
 */
@Component
@Scope("prototype")
public class EnsureMasterStep extends MySQLNodeCheckStep {
    @Override
    public NodeCheckStepResult internalCheck(MySqlServiceConfig config) {
        if(NodeCheckContext.get("master") == null) {
            return NodeCheckStepResult.ERROR;
        }
        return NodeCheckStepResult.SUCCEEDED;
    }

    @Override
    public String rootCause() {
        return "Master not set on cluster!";
    }

    @Override
    public String action() {
        return "Check if cluster has a master";
    }

    @Override
    protected String getTemplateName() {
        return "MasterNotSet";
    }
}
