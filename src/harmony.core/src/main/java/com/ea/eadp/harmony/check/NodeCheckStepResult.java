/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

/**
 * Created by VincentZhang on 5/22/2018.
 */
public enum NodeCheckStepResult {
    SUCCEEDED,  // Succeeded, no more information
    WARNING,    // Sensed some error, but can still continue
    ERROR,      // Can't continue, need to stop there
}
