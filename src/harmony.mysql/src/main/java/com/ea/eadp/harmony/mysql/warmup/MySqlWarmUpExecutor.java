/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.warmup;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import com.ea.eadp.harmony.warmup.NodeWarmUpExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class MySqlWarmUpExecutor extends ServiceSupport implements NodeWarmUpExecutor<MySqlServiceConfig> {
    private final static Logger logger = LoggerFactory.getLogger(MySqlWarmUpExecutor.class);

    private final static String SWITCH_ENABLE = "enable";
    private static final String WARM_UP_TEMPLATE = "use identity;"
            + "SET sql_log_bin=0;"
            + "create table temp_{{table}} like {{table}};"
            + "alter table temp_{{table}} engine=blackhole;"
            + "insert into temp_{{table}} select * from {{table}};"
            + "drop table temp_{{table}};"
            + "SET sql_log_bin =1";
    private static final String WARM_UP_DROP_INDEX_TEMPLATE = "use identity;"
            + "SET sql_log_bin=0;"
            + "create table temp_{{table}} like {{table}};"
            + "alter table temp_{{table}} {{drop_index}};"
            + "alter table temp_{{table}} engine=blackhole;"
            + "insert into temp_{{table}} select * from {{table}};"
            + "drop table temp_{{table}};"
            + "SET sql_log_bin =1";

    @Value("${service.mysql.user}")
    private String mysqlUser;

    @Autowired
    private ClusterManager clusterManager;

    @Override
    public void warmUpNode(MySqlServiceConfig config) {
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);
        try {
            String[] tables = {
                    "user_privacy_setting", "machine_profile_v2", "user_phone_number", "user_password_v2", "email_user_lookup", "user_account",
                    "delegate_token", "user_account_auth", "pid_game_persona_mapping", "persona_name_persona_lookup", "persona", "persona_auth",
                    "device_persona_mapping", "user_property", "twofactor_authentication", "user_optins", "persona_extref", "pid_user_ext_ref",
                    "persona_extref_ext", "persona_property", "entitlement_auth_persona", "pid_persona_ext_ref", "user_security_qstn",
                    "user_profile", "user_profile_svy"
            };
            ZooKeeperService zkSvc = getZooKeeperService();
            for (String table : tables) {
                String curSlave = clusterManager.getCurrentPrimarySlave(config.getService());
                if (!clusterManager.getCurrentNode().equals(curSlave)) {
                    logger.info("Become master now. Stop warm up");
                    break;
                }
                String switchState = zkSvc.getNodeStringData(clusterManager.getWarmUpSwitchPath());
                if (switchState == null || !switchState.equalsIgnoreCase(SWITCH_ENABLE)) {
                    logger.info("Warm up is disabled. Stop running queries.");
                    break;
                }
                String sqls;
                switch (table) {
                    case "persona_name_persona_lookup":
                        sqls = WARM_UP_DROP_INDEX_TEMPLATE
                                .replace("{{drop_index}}", "drop index PERSONA_NAME_IDX")
                                .replace("{{table}}", table);
                        break;
                    case "persona":
                        sqls = WARM_UP_DROP_INDEX_TEMPLATE
                                .replace("{{drop_index}}", "drop index PERSONA_DISPLAYNAME_IDX, drop index PERSONA_NAME_IDX")
                                .replace("{{table}}", table);
                        break;
                    default:
                        sqls = WARM_UP_TEMPLATE.replace("{{table}}", table);
                }
                mySqlWrapper.execute(new CommandNoReturn<Connection>() {
                    @Override
                    public void execute(Connection client) {
                        MySqlWrapper.runWarmUp(client, sqls.split(";"));
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Failed to run warm-up queries", e);
        }
    }

    @Override
    public void killWarmUp(MySqlServiceConfig config) {
        MySqlWrapper mySqlWrapper = new MySqlWrapper(config);
        try {
            mySqlWrapper.execute(new CommandNoReturn<Connection>() {
                @Override
                public void execute(Connection client) {
                    MySqlWrapper.killWarmUp(client, mysqlUser);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to kill warm-up queries", e);
        }
    }
}
