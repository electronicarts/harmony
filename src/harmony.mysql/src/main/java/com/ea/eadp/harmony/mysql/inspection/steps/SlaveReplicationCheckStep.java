/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.inspection.steps;

import com.ea.eadp.harmony.check.NodeCheckContext;
import com.ea.eadp.harmony.check.NodeCheckStepResult;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.entity.SlaveStatusDB;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Created by VincentZhang on 5/29/2018.
 */
@Component
@Scope("prototype")
public class SlaveReplicationCheckStep extends MySQLNodeCheckStep {
    private final static Logger logger = LoggerFactory.getLogger(SlaveReplicationCheckStep.class);
    private String rootCause = "Replication is not up to date";

    @Override
    public NodeCheckStepResult internalCheck(MySqlServiceConfig config) {
        boolean curNodeIsMaster = false;
        String currMaster = (String) NodeCheckContext.get("master");
        if (currMaster.equals(config.getNode())) {
            curNodeIsMaster = true;
        }

        SlaveStatusDB slaveStatusDB = new SlaveStatusDB();
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);
        try {
            Object statusDB = mySqlWrapper.execute(new Command<Connection, SlaveStatusDB>() {
                @Override
                public SlaveStatusDB execute(Connection client) {
                    SlaveStatusDB ret = MySqlWrapper.getSlaveStatus(client);
                    return ret;
                }
            });
            logger.info("Got " + statusDB);

            // check replication delay
            slaveStatusDB = (SlaveStatusDB) statusDB;
            logger.info("Replication delay: " + slaveStatusDB.getSecondsBehindMaster());

            // check slave threads
            rootCause = "MySQL replication IO thread is not running";
            if (slaveStatusDB.getSlaveIoRunning().equals("No")) {
                throw new RuntimeException(rootCause);
            }

            rootCause = "MySQL replication SQL thread is not running";
            if (slaveStatusDB.getSlaveSqlRunning().equals("No")) {
                throw new RuntimeException(rootCause);
            }

            rootCause = "Everything good";
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            if (curNodeIsMaster) {
                // If this node is master, didn't take offline to prevent potential issue.
                return NodeCheckStepResult.WARNING;
            } else {
                // If this is a slave node and replication status error, took offline.
                return NodeCheckStepResult.ERROR;
            }
        } finally {
            if (slaveStatusDB != null) {
                // set secondsBehindMaster
                String atPath = getClusterManager().getPropertiesPath(config.getService(), config.getNode());
                ZooKeeperService zkSvc = getZooKeeperService();
                zkSvc.ensurePath(atPath + "/secondsBehindMaster");
                zkSvc.setNodeLongData(atPath + "/secondsBehindMaster", slaveStatusDB.getSecondsBehindMaster());

                zkSvc.ensurePath(atPath + "/slaveIoRunning");
                zkSvc.setNodeStringData(atPath + "/slaveIoRunning", slaveStatusDB.getSlaveIoRunning());

                zkSvc.ensurePath(atPath + "/slaveSqlRunning");
                zkSvc.setNodeStringData(atPath + "/slaveSqlRunning", slaveStatusDB.getSlaveSqlRunning());

                // set secondsBehindMaster
                ServiceNodeStatus masterStatus = getClusterManager().getServiceNodeStatus(config.getService(),
                        currMaster);
                if (masterStatus == ServiceNodeStatus.ONLINE) {
                    long masterZxid = getClusterManager().getMasterZxid(config.getService());
                    // set zxidOfMaster when master is also online
                    zkSvc.ensurePath(atPath + "/zxidOfMaster");
                    zkSvc.setNodeLongData(atPath + "/zxidOfMaster", masterZxid);
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
        return "Please check replication status of this node";
    }

    @Override
    protected String getTemplateName() {
        return "SlaveReplicationError";
    }
}
