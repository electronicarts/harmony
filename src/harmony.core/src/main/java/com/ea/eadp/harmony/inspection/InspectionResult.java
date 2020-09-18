/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.inspection;

import com.ea.eadp.harmony.control.ServiceNodeStatus;

/**
 * Created by leilin on 10/16/2014.
 */
public class InspectionResult {
    private ServiceNodeStatus status;

    public InspectionResult(ServiceNodeStatus status) {
        this.status = status;
    }

    public ServiceNodeStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "InspectionResult{" +
                "status=" + status +
                '}';
    }
}