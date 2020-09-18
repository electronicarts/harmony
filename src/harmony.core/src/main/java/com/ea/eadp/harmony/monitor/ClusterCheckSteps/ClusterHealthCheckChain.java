/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.monitor.ClusterCheckSteps;

import com.ea.eadp.harmony.check.CheckStepChain;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by VincentZhang on 5/21/2018.
 */
@Component
public class ClusterHealthCheckChain extends CheckStepChain implements InitializingBean {
    @Autowired
    ClearContextStep clearContextStep;
    @Autowired
    HarmonyRunningCountCheck runningCountCheck;

    @Override
    public void afterPropertiesSet() throws Exception {
        setFirstStep(runningCountCheck);
        setFinalStep(clearContextStep);
    }
}
