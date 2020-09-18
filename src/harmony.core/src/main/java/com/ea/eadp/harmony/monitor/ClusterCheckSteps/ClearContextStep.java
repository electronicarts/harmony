/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor.ClusterCheckSteps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStep;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class ClearContextStep extends NodeCheckStep {
    @Override
    public NodeCheckStepResult check(Map dataObjectMap) {
        NodeCheckContext.clear();
        return NodeCheckStepResult.SUCCEEDED;
    }

    @Override
    public String rootCause() {
        return null;
    }

    @Override
    public String action() {
        return null;
    }

    @Override
    protected String getTemplateName() {
        return "N/A";
    }
}
