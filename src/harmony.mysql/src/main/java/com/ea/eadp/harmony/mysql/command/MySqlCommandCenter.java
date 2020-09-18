/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.command;

import com.ea.eadp.harmony.command.CommandHandler;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.vip.VipCommandCenter;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.entity.MasterLocation;
import com.ea.eadp.harmony.mysql.entity.MasterStatusDB;
import com.ea.eadp.harmony.mysql.entity.MySqlAction;
import com.ea.eadp.harmony.mysql.entity.StartSlaveUntil;
import com.ea.eadp.harmony.mysql.utils.MySqlWrapper;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import com.ea.eadp.harmony.shared.utils.ShellWrapper;
import com.ea.eadp.harmony.shared.utils.ShellWrapper.ExitRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Created by leilin on 10/29/2014.
 */
@Component
public class MySqlCommandCenter extends VipCommandCenter {
    private final static Logger logger = LoggerFactory.getLogger(MySqlCommandCenter.class);

    @Value("${service.mysql.user}")
    private String mysqlUser;

    @CommandHandler(PingCommand.class)
    public PingResponse ping(PingCommand command) {
        return new PingResponse(command.getMessage());
    }

    @CommandHandler(MySqlCommand.class)
    public HarmonyCommandResult handleMySqlCommand(MySqlCommand command) {
        return handle(command);
    }

    @CommandHandler(MySqlLocationCommand.class)
    public HarmonyCommandResult handleMySqlLocationCommand(MySqlLocationCommand command) {
        return handle(command);
    }

    @CommandHandler(StartSlaveCommand.class)
    public HarmonyCommandResult handleStartSlaveCommand(StartSlaveCommand command) {
        return handle(command);
    }

    private HarmonyCommandResult handle(MySqlCommand command) {
        ServiceEnvironment serviceEnvironment = command.getTarget();
        MySqlServiceConfig mySqlServiceConfig = (MySqlServiceConfig) serviceConfigRepository.getServiceConfig(serviceEnvironment);
        MySqlWrapper mySqlWrapper = new MySqlWrapper(mySqlServiceConfig);

        MySqlAction mySqlAction = command.getAction();
        String resultMessage = null;
        switch (mySqlAction) {
            case START:
                // fall through
            case RESTART:
                // fall through
            case STOP:
                ExitRecord res = ShellWrapper.executeCmd(genStatement(command));
                resultMessage = "Got " + res + " for " + command;
                break;
            case CLEAR_READ_ONLY:
                configReadOnly(serviceEnvironment, 0);
                resultMessage = "Done " + command;
                break;
            case SET_READ_ONLY:
                configReadOnly(serviceEnvironment, 1);
                resultMessage = "Done " + command;
                break;
            case GET_MASTER_STATUS:
                MasterStatusDB status = mySqlWrapper.execute(new Command<Connection, MasterStatusDB>() {
                    @Override
                    public MasterStatusDB execute(Connection client) {
                        MasterStatusDB ret = MySqlWrapper.getMasterStatus(client);
                        return ret;
                    }
                });
                resultMessage = "Got " + status;
                // construct the response
                logger.info(resultMessage);
                MasterStatusResponse ret = new MasterStatusResponse(status);
                ret.setResultMessage(resultMessage);
                return ret;
            case WAIT_MASTER_POS:
                // fall through
            case START_SLAVE:
                // fall through
            case STOP_SLAVE:
                // fall through
            case RESET_SLAVE_ALL:
                final String stmt = genStatement(command);
                boolean succ = executeStatement(mySqlWrapper, stmt);
                resultMessage = "Got " + succ + " for " + stmt;
                break;
            case KILL_CONNECTIONS:
                final String applicationUser = mySqlServiceConfig.getApplicationUser();
                int rc = mySqlWrapper.execute(new Command<Connection, Integer>() {
                    @Override
                    public Integer execute(Connection client) {
                        Integer ret = MySqlWrapper.killConnections(client, applicationUser);
                        return ret;
                    }
                });
                resultMessage = "Killed " + rc + " connections";
                break;
            case ONLINE_SLAVE:
                onlineSlave(serviceEnvironment, 1, ((MySqlLocationCommand) command).getLocation());
                resultMessage = "Done " + command;
                break;
            case ASSIGN_PRIMARY:
                onlineSlave(serviceEnvironment, 0, ((MySqlLocationCommand) command).getLocation());
                resultMessage = "Done " + command;
                break;
            case KILL_WARMUP:
                mySqlWrapper.execute(new CommandNoReturn<Connection>() {
                    @Override
                    public void execute(Connection client) {
                        MySqlWrapper.killWarmUp(client, mysqlUser);
                    }
                });
                resultMessage = "Killed warm up queries";
                break;
            default:
        }
        // construct the response
        logger.info(resultMessage);
        HarmonyCommandResult ret = new HarmonyCommandResult();
        ret.setResultMessage(resultMessage);
        return ret;
    }

    private String genStatement(MySqlCommand command) {
        MasterLocation masterLocation = (command instanceof MySqlLocationCommand) ?
                ((MySqlLocationCommand) command).getLocation() : null;

        ServiceEnvironment serviceEnvironment = command.getTarget();
        MySqlServiceConfig mySqlServiceConfig = (MySqlServiceConfig) serviceConfigRepository.getServiceConfig(serviceEnvironment);

        MySqlAction mySqlAction = command.getAction();
        switch (mySqlAction) {
            case START:
                return "service mysql start";
            case RESTART:
                return "service mysql restart --skip-slave-start";
            case STOP:
                return "service mysql stop";
            case CLEAR_READ_ONLY:
                return "set global read_only=0";
            case SET_READ_ONLY:
                return "set global read_only=1";
            case WAIT_MASTER_POS:
                return formatStatement(
                        "select master_pos_wait('%s', %d)",
                        masterLocation.getFile(),
                        masterLocation.getPosition());
            case START_SLAVE:
                StartSlaveUntil until = ((StartSlaveCommand) command).getUntil();
                String logFile = null;
                String logPos = null;
                String untilClause;
                switch (until) {
                    case FOR_MASTER:
                        logFile = "master_log_file";
                        logPos = "master_log_pos";
                        break;
                    case FOR_RELAY:
                        logFile = "relay_log_file";
                        logPos = "relay_log_pos";
                        break;
                }
                switch (until) {
                    case FOR_EVER:
                        untilClause = "";
                        break;
                    default:
                        untilClause = formatStatement(
                                " until %s='%s', %s=%d",
                                logFile,
                                masterLocation.getFile(),
                                logPos,
                                masterLocation.getPosition());
                }
                return "start slave" + untilClause;
            case STOP_SLAVE:
                return "stop slave";
            case RESET_SLAVE_ALL:
                return "reset slave all";
            // case GET_MASTER_STATUS:
            // case KILL_CONNECTIONS:
            // case ONLINE_SLAVE:
            // case ASSIGN_PRIMARY:
            default:
                throw new RuntimeException("Can't genStatement for " + mySqlAction);
        }
    }

    private String formatStatement(String tmplt, Object... args) {
        return String.format(tmplt, args);
    }

    private boolean executeStatement(final MySqlWrapper mySqlWrapper, final String stmt) {
        return mySqlWrapper.execute(new Command<Connection, Boolean>() {
            @Override
            public Boolean execute(Connection client) {
                Boolean ret = MySqlWrapper.executeStatement(client, stmt);
                return ret;
            }
        });
    }

    private void configReadOnly(ServiceEnvironment serviceEnvironment, int value) {
        // Do nothing. This is the existing behavior on INT and PROD.
        if (true) return;

        MySqlServiceConfig mySqlServiceConfig = (MySqlServiceConfig) serviceConfigRepository.getServiceConfig(serviceEnvironment);
        MySqlWrapper mySqlWrapper = new MySqlWrapper(mySqlServiceConfig);

        MySqlCommand command = null;
        switch (value) {
            case 0:
                command = new MySqlCommand(serviceEnvironment, MySqlAction.CLEAR_READ_ONLY);
                break;
            case 1:
                command = new MySqlCommand(serviceEnvironment, MySqlAction.SET_READ_ONLY);
                break;
            default:
        }
        executeStatement(mySqlWrapper, genStatement(command));

        String baseDir = System.getProperty("user.dir");

        ShellWrapper.executeCmd(formatStatement(
                "%s/bin/config_mysql %s read_only %d",
                baseDir,
                mySqlServiceConfig.getMysqlCnf(),
                value
        ));
    }

    private void onlineSlave(ServiceEnvironment serviceEnvironment, int readOnly, MasterLocation location) {
        MySqlCommand command = new StartSlaveCommand(serviceEnvironment, MySqlAction.START_SLAVE, null, StartSlaveUntil.FOR_EVER);
        handle(command);
    }
}
