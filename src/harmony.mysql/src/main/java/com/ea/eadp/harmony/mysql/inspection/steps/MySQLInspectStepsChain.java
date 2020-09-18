/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.CheckStepChain;
import com.ea.eadp.harmony.monitor.ClusterCheckSteps.ClearContextStep;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 5/28/2018.
 */
@Component
@Scope("prototype")
public class MySQLInspectStepsChain extends CheckStepChain implements InitializingBean{
    @Autowired
    ClearContextStep clearContextStep;

    @Autowired
    ServiceRunningStep serviceRunningStep;

    @Autowired
    MasterReplicationStatusCheckStep masterReplicationStatusCheckStep;

    @Autowired
    WriteMarkerToZkprStep writeMarkerToZkprStep;

    @Autowired
    EnsureMasterStep ensureMasterStep;
    @Autowired
    SlaveReplicationCheckStep slaveReplicationCheckStep;

    @Override
    public void afterPropertiesSet() throws Exception {
        setFirstStep(ensureMasterStep).
                setNext(serviceRunningStep).
                setNext(masterReplicationStatusCheckStep).
                setNext(slaveReplicationCheckStep);

        setFinalStep(writeMarkerToZkprStep).setNext(clearContextStep);
    }
}


