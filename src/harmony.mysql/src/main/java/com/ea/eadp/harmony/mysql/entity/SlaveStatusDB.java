/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.entity;

/**
 * Created by juding on 10/27/2014.
 */
public class SlaveStatusDB {
    private String masterLogFile;
    private Long execMasterLogPos;
    private Long secondsBehindMaster;
    private String slaveIoRunning;
    private String slaveSqlRunning;

    public SlaveStatusDB() {
        execMasterLogPos = -1L;
        execMasterLogPos = -1L;
        slaveIoRunning = "Unknown";
        slaveSqlRunning = "Unknown";
        masterLogFile = "Unknown";
    }

    public SlaveStatusDB(String masterLogFile, Long execMasterLogPos, Long secondsBehindMaster, String slaveIoRunning, String slaveSqlRunning) {
        this.masterLogFile = masterLogFile;
        this.execMasterLogPos = execMasterLogPos;
        this.secondsBehindMaster = secondsBehindMaster;
        this.slaveIoRunning = slaveIoRunning;
        this.slaveSqlRunning = slaveSqlRunning;
    }

    public String getMasterLogFile() {
        return masterLogFile;
    }

    public void setMasterLogFile(String masterLogFile) {
        this.masterLogFile = masterLogFile;
    }

    public Long getExecMasterLogPos() {
        return execMasterLogPos;
    }

    public void setExecMasterLogPos(Long execMasterLogPos) {
        this.execMasterLogPos = execMasterLogPos;
    }

    public Long getSecondsBehindMaster() {
        return secondsBehindMaster;
    }

    public void setSecondsBehindMaster(Long secondsBehindMaster) {
        this.secondsBehindMaster = secondsBehindMaster;
    }

    public String getSlaveIoRunning() {
        return slaveIoRunning;
    }

    public void setSlaveIoRunning(String slaveIoRunning) {
        this.slaveIoRunning = slaveIoRunning;
    }

    public String getSlaveSqlRunning() {
        return slaveSqlRunning;
    }

    public void setSlaveSqlRunning(String slaveSqlRunning) {
        this.slaveSqlRunning = slaveSqlRunning;
    }

    @Override
    public String toString() {
        return "SlaveStatusDB{" +
                "masterLogFile='" + masterLogFile + '\'' +
                ", execMasterLogPos=" + execMasterLogPos +
                ", secondsBehindMaster=" + secondsBehindMaster +
                ", slaveIoRunning='" + slaveIoRunning + '\'' +
                ", slaveSqlRunning='" + slaveSqlRunning + '\'' +
                '}';
    }
}
