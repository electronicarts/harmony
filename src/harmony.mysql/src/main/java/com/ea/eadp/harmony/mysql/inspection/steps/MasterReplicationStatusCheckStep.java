/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.entity.MasterStatusDB;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import com.ea.eadp.harmony.shared.utils.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by VincentZhang on 5/29/2018.
 */
@Component
@Scope("prototype")
public class MasterReplicationStatusCheckStep extends MySQLNodeCheckStep {
    private final static Logger logger = LoggerFactory.getLogger(MasterReplicationStatusCheckStep.class);

    @Override
    public NodeCheckStepResult internalCheck(MySqlServiceConfig config) {
        String service = config.getService();

        long masterZxid = getClusterManager().getMasterZxid(service);
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);
        Connection conn = null;
        try {
            conn = mySqlWrapper.getConnection();

            if (conn != null) {
                Object queryResult = mySqlWrapper.execute(new Command<Connection, MasterStatusDB>() {
                    @Override
                    public MasterStatusDB execute(Connection client) {
                        MasterStatusDB ret = MySqlWrapper.getMasterStatus(client);
                        return ret;
                    }
                });
                logger.info("Got " + queryResult);
            }
        } catch (Exception e) {
            logger.error("Exception happened when trying to retrieve connection from MySQL.", e);
            return NodeCheckStepResult.ERROR;
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
        return "Master replication error";
    }

    @Override
    public String action() {
        return "Please check master replication status";
    }

    @Override
    protected String getTemplateName() {
        return "MasterReplication";
    }
}
