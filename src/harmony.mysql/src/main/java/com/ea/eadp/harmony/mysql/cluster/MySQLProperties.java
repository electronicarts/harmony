/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

/**
 * Created by VincentZhang on 5/3/2018.
 */
public class MySQLProperties {
    public String secondsBehindMaster;
    public String slaveIoRunning;
    public String slaveSqlRunning;

    public String getSecondsBehindMaster() {
        return secondsBehindMaster;
    }

    public String getSlaveIoRunning() {
        return slaveIoRunning;
    }

    public String getSlaveSqlRunning() {
        return slaveSqlRunning;
    }
}
