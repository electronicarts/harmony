/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by VincentZhang on 5/29/2018.
 */
public class CheckStepChain implements CheckStep {
    private final static Logger logger = LoggerFactory.getLogger(CheckStepChain.class);

    private NodeCheckStep firstStep;
    private NodeCheckStep finalStep;

    public NodeCheckStep setFirstStep(NodeCheckStep firstStep) {
        this.firstStep = firstStep;
        return firstStep;
    }

    // Final steps are steps that will be executed no matter what happened in normal steps.
    public NodeCheckStep setFinalStep(NodeCheckStep finalStep) {
        this.finalStep = finalStep;
        return finalStep;
    }

    @Override
    public NodeCheckStepResult handle() {
        try {
            if (firstStep != null) {
                return firstStep.handle();
            }
        } catch (Exception e) {
            logger.error("Unexcepted exception happened", e);
        } finally {
            if (finalStep != null) {
                finalStep.handle();
            }
        }

        // Shouldn't get there!
        return NodeCheckStepResult.ERROR;
    }
}
