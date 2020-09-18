/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.config;

import com.ea.eadp.harmony.config.AutoFailoverServiceConfig;
import com.ea.eadp.harmony.config.annotation.ServiceProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leilin on 10/15/2014.
 */
public class MySqlServiceConfig extends AutoFailoverServiceConfig {

    @ServiceProperty("service.mysql.user")
    private String monitorUser;

    @ServiceProperty("service.mysql.password")
    private String monitorPassword;

    @ServiceProperty("service.mysql.applicationUser")
    private String applicationUser;

    @ServiceProperty("service.mysql.mysqlCnf")
    private String mysqlCnf;

    @ServiceProperty("service.mysql.timeoutSync")
    private long timeoutSync;

    @ServiceProperty("service.mysql.useSSL")
    private String useSSL;

    @ServiceProperty("service.mysql.requireSSL")
    private String requireSSL;

    @ServiceProperty("service.mysql.verifyServerCertificate")
    private String verifyServerCertificate;

    public String getMonitorUser() {
        return monitorUser;
    }

    public void setMonitorUser(String monitorUser) {
        this.monitorUser = monitorUser;
    }

    public String getMonitorPassword() {
        return monitorPassword;
    }

    public void setMonitorPassword(String monitorPassword) {
        this.monitorPassword = monitorPassword;
    }

    public String getApplicationUser() {
        return applicationUser;
    }

    public void setApplicationUser(String applicationUser) {
        this.applicationUser = applicationUser;
    }

    public String getMysqlCnf() {
        return mysqlCnf;
    }

    public void setMysqlCnf(String mysqlCnf) {
        this.mysqlCnf = mysqlCnf;
    }

    public long getTimeoutSync() {
        return timeoutSync;
    }

    public void setTimeoutSync(long timeoutSync) {
        this.timeoutSync = timeoutSync;
    }

    public String getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(String useSSL) {
        this.useSSL = useSSL;
    }

    public String getRequireSSL() {
        return requireSSL;
    }

    public void setRequireSSL(String requireSSL) {
        this.requireSSL = requireSSL;
    }

    public String getVerifyServerCertificate() {
        return verifyServerCertificate;
    }

    public void setVerifyServerCertificate(String verifyServerCertificate) {
        this.verifyServerCertificate = verifyServerCertificate;
    }

    public Map<String, String> getJdbcUrlParams() {
        Map<String, String> params = new HashMap<>();
        params.put("useSSL", useSSL);
        params.put("requireSSL", requireSSL);
        params.put("verifyServerCertificate", verifyServerCertificate);
        return params;
    }
}
