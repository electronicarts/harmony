/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

/**
 * User: leilin
 * Date: 10/2/14
 */
public class Role {
    public String master;
    public String primary_slave;

    public String getMaster() {
        return master;
    }

    public String getPrimary_slave() {
        return primary_slave;
    }
}
