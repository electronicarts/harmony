/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

/**
 * Created by juding on 11/7/14.
 */
public class CheckResult {
    private ServiceNodeHealth health;

    public CheckResult(ServiceNodeHealth health) {
        this.health = health;
    }

    public ServiceNodeHealth getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "health=" + health +
                '}';
    }
}
