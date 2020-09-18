/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.check;

import com.ea.eadp.harmony.check.CheckResult;
import com.ea.eadp.harmony.check.NodeChecker;
import com.ea.eadp.harmony.check.ServiceNodeHealth;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import com.ea.eadp.harmony.shared.utils.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Created by juding on 11/7/14.
 */
@Component
public class MySqlChecker implements NodeChecker<MySqlServiceConfig> {
    private final static Logger logger = LoggerFactory.getLogger(MySqlChecker.class);

    @Override
    public CheckResult checkNode(MySqlServiceConfig config) {
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);
        ServiceNodeHealth health = ServiceNodeHealth.HEALTH_OK;
        try {
            String stringDB = mySqlWrapper.execute(new Command<Connection, String>() {
                @Override
                public String execute(Connection client) {
                    String ret = MySqlWrapper.getSystemVariable(client, "server_id");
                    return ret;
                }
            });
            logger.info("Got " + stringDB);
            long serverId = Long.parseLong(stringDB);
            if (serverId <= 0)
                throw new RuntimeException("Invalid server_id " + serverId);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            health = ServiceNodeHealth.HEALTH_ERROR;
        }

        CheckResult checkResult = new CheckResult(health);
        logger.info("Got " + checkResult);

        return checkResult;
    }
}
