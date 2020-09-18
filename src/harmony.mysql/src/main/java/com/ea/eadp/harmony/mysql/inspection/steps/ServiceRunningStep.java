/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by VincentZhang on 5/28/2018.
 */
@Component
@Scope("prototype")
public class ServiceRunningStep extends MySQLNodeCheckStep {
    private final static Logger logger = LoggerFactory.getLogger(ServiceRunningStep.class);

    private String templateName = "MySQLServiceRunning";

    private String rootCause = "MySQL service is not running.";
    private String action = "Check and start up the MySQL.";

    @Override
    public NodeCheckStepResult internalCheck(MySqlServiceConfig config) {
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);

        Connection conn = null;
        try {
            conn = mySqlWrapper.getConnection();
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                SQLException sqlException = (SQLException) e.getCause();
                if (sqlException.getErrorCode() == 1130 ||   // Can't connect from remote
                        sqlException.getErrorCode() == 2002 ||  // Can't connect from local
                        sqlException.getErrorCode() == 1045) { // Password error
                    rootCause = "Permission issue";
                    action = "Harmony doesn't have the right permission to connect to MySQL. " +
                            "Check and give Harmony user:" + config.getMonitorUser() + " the permission to connect to the target from both " +
                            "remote/local and grant it correct permissions.";
                } else {
                    rootCause = "SQL error happened while trying to connect to MySQL";
                    action = "SQL Exception code:" + sqlException.getErrorCode() + " Detailed message:" + sqlException.getMessage() +
                            ". Please check the error code and msg and fix the error in MySQL service side";
                }
            }
            logger.error("Exception happened when trying to retrieve connection from MySQL.", e);
            conn = null;
            return NodeCheckStepResult.ERROR; // Can't get connection, no need to further check service
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Exception happened when trying to close connection to MySQL.", e);
                    return NodeCheckStepResult.ERROR;
                }
            }
        }
        return NodeCheckStepResult.SUCCEEDED;
    }

    @Override
    public String rootCause() {
        return rootCause;
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    protected String getTemplateName() {
        return templateName;
    }
}
