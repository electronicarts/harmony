/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.utils;

import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.ResultType;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.shared.utils.ShellWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisFailoverCommandWrapper extends RedisCommandWrapper {
    private String masterHost = null;
    private Integer masterPort = null;
    private final static Logger logger = LoggerFactory.getLogger(RedisFailoverCommandWrapper.class);

    final private static String CHANGE_MASTER = "slaveof";

    final private static String REPLICATION_OK = "OK";

    public RedisFailoverCommandWrapper(RedisServiceConfig config, RedisServiceConfig masterConfig) {
        super(config);
        if (masterConfig.equals(config)) {
            this.masterHost = "NO ONE";
            this.masterPort = null;
        } else {
            this.masterHost = (masterConfig.getFailoverInterface() == null || "_unknown".equals(masterConfig.getFailoverInterface())) ?
                    masterConfig.getHost() : masterConfig.getFailoverInterface();
            this.masterPort = masterConfig.getPort();
        }
    }

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public Integer getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(Integer masterPort) {
        this.masterPort = masterPort;
    }

    public HarmonyCommandResult changeRedisMaster() {
        String cmd = String.format(redisCommandPrefix + CHANGE_MASTER + " %s", masterHost);
        if (masterPort != null) {
            cmd = cmd + " " + masterPort;
        }
        logger.info("Ready to do failover:" + cmd);
        ShellWrapper.ExitRecord res = ShellWrapper.executeCmd(false, cmd);
        if (res.success() && res.outStr.equals(REPLICATION_OK)) {
            return new HarmonyCommandResult();
        } else {
            HarmonyCommandResult harmonyRes = new HarmonyCommandResult();
            harmonyRes.setResultType(ResultType.FAILED);
            harmonyRes.setErrorMessage(res.errStr);
            harmonyRes.setResultMessage(res.outStr);
            return harmonyRes;
        }
    }
}
