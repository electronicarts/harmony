/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor;

import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.inspection.InspectionResult;

/**
 * Created by juding on 10/23/2014.
 */
public class MonitorResult extends InspectionResult {
    public MonitorResult(ServiceNodeStatus status) {
        super(status);
    }

    @Override
    public String toString() {
        return "MonitorResult{" +
                "status=" + getStatus() +
                '}';
    }
}
