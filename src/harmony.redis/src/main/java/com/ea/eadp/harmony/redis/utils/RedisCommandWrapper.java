/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.utils;

import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.ResultType;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.redis.command.RedisReplicationCommandResult;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.shared.utils.ShellWrapper;
import com.ea.eadp.harmony.shared.utils.ShellWrapper.ExitRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandWrapper {
    private final static Logger logger = LoggerFactory.getLogger(RedisCommandWrapper.class);

    private String hostname;
    private Integer port;
    private String password;
    private String installPath;

    protected String redisCommandPrefix;
    final private static String CHECK_HEALTH_CMD = "ping";
    final private static String CHECK_REPLICA_CMD = "info replication";
    final private static String KILL_CONNECTION = "client kill type normal";

    final private static String HEALTH_OK = "PONG";

    public RedisCommandWrapper() {
    }

    public RedisCommandWrapper(RedisServiceConfig redisConfig) {
        this(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getRedisPassword(), redisConfig.getRedisIntallPath());
    }

    public RedisCommandWrapper(String hostname, Integer port, String password, String installPath) {
        this.hostname = hostname;
        this.port = port;
        this.password = password;
        this.installPath = installPath;
        this.redisCommandPrefix = String.format("%sredis-cli -h %s -p %d -a %s ", this.installPath, this.hostname, this.port, this.password);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ServiceNodeStatus CheckRedisHealth() {
        ExitRecord res = ShellWrapper.executeCmd(false, redisCommandPrefix + CHECK_HEALTH_CMD);
        if (res.success() && res.outStr.equals(HEALTH_OK)) {
            return ServiceNodeStatus.ONLINE;
        } else {
            return ServiceNodeStatus.DOWN;
        }
    }

    public RedisReplicationCommandResult checkRedisReplication() {
        ExitRecord res = ShellWrapper.executeCmd(false, redisCommandPrefix + CHECK_REPLICA_CMD);
        logger.info(String.format("replication status on %s:%d:%s", hostname, port, res.outStr));
        if (res.success()) {
            RedisReplicationCommandResult cmdRes = new RedisReplicationCommandResult();
            cmdRes.setRedisReplicationStatusEntity(RedisReplicationStatusEntityConverter.convert(res.outStr));
            return cmdRes;
        } else {
            RedisReplicationCommandResult cmdRes = new RedisReplicationCommandResult();
            cmdRes.setResultType(ResultType.FAILED);
            cmdRes.setErrorMessage(res.errStr);
            cmdRes.setResultMessage(res.outStr);

            return cmdRes;
        }
    }

    public HarmonyCommandResult killRedisConnections() {
        ExitRecord res = ShellWrapper.executeCmd(false, redisCommandPrefix + KILL_CONNECTION);
        if (res.success() && !res.outStr.contains("error")) {
            HarmonyCommandResult harmonyRes = new HarmonyCommandResult();
            harmonyRes.setResultType(ResultType.SUCCEEDED);
            harmonyRes.setErrorMessage(res.errStr);
            harmonyRes.setResultMessage(res.outStr);
            return harmonyRes;
        } else {
            HarmonyCommandResult harmonyRes = new HarmonyCommandResult();
            harmonyRes.setResultType(ResultType.FAILED);
            harmonyRes.setErrorMessage(res.errStr);
            harmonyRes.setResultMessage(res.outStr);
            return harmonyRes;
        }
    }
}
