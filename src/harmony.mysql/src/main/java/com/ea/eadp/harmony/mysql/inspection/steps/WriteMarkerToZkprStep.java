/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStep;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by VincentZhang on 5/29/2018.
 */
@Component
@Scope("prototype")
public class WriteMarkerToZkprStep extends NodeCheckStep {
    @Override
    public NodeCheckStepResult check(Map dataObjectMap) {
        MySqlServiceConfig config = (MySqlServiceConfig) NodeCheckContext.get("config");
        String service = config.getService();
        String node = config.getNode();

        String markerStepPath = getClusterManager().getMarkerStepPath(service, node);
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.ensurePath(markerStepPath);

        NodeCheckStep breakPoint = (NodeCheckStep) NodeCheckContext.get("marker");
        if (breakPoint == null) {
            breakPoint = this;
        }

        // Format: clzname##rootCause##action
        String resultString = breakPoint.getClass().getName() + "##" + breakPoint.rootCause() + "##" + breakPoint.action();
        zkSvc.setNodeStringData(markerStepPath, resultString);
        return NodeCheckStepResult.SUCCEEDED;
    }

    @Override
    public String rootCause() {
        return "OK";
    }

    @Override
    public String action() {
        return "Everything good, no action.";
    }

    @Override
    protected String getTemplateName() {
        return null;
    }
}
