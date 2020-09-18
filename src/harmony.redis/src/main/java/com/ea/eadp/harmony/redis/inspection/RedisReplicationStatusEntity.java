/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.inspection;

import com.ea.eadp.harmony.redis.utils.StringConstants;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class RedisReplicationStatusEntity {
    private static Set<String> propertyNames = new HashSet<String>() {{
        add(StringConstants.REPL_BACKLOG_ACTIVE);
        add(StringConstants.REPL_BACKLOG_FIRST_BYTE_OFFSET);
        add(StringConstants.REPL_BACKLOG_HISLEN);
        add(StringConstants.REPL_BACKLOG_SIZE);
        add(StringConstants.REPL_CONNECT_SLAVES);
        add(StringConstants.REPL_MASTER_HOST);
        add(StringConstants.REPL_MASTER_LAST_IO_SECONDS_AGO);
        add(StringConstants.REPL_MASTER_LINK_STATUS);
        add(StringConstants.REPL_MASTER_OFFSET);
        add(StringConstants.REPL_MASTER_PORT);
        add(StringConstants.REPL_MASTER_SYNC_IN_PROGRESS);
        add(StringConstants.REPL_ROLE);
        add(StringConstants.REPL_SLAVE_OFFSET);
        add(StringConstants.REPL_SLAVE_PRIORITY);
        add(StringConstants.REPL_SLAVE_READ_ONLY);
    }};

    private RedisReplicationRole redisReplicationRole;
    private Map<String, String> properties = new HashMap<String, String>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getRedisReplicationProperty(String name) {
        if (propertyNames.contains(name)) {
            return properties.get(name);
        } else {
            return null;
        }
    }

    public void setRedisReplicationProperty(String name, String value) {
        if(propertyNames.contains(name)) {
            properties.put(name, value);
            if (name.equals(StringConstants.REPL_ROLE)) {
                if (value.equals("master")) {
                    redisReplicationRole = RedisReplicationRole.MASTER;
                } else if (value.equals("slave")) {
                    redisReplicationRole = RedisReplicationRole.SLAVE;
                }
            }
        }
    }

    public RedisReplicationRole getRedisReplicationRole() {
        return redisReplicationRole;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Map.Entry entry:properties.entrySet()) {
            builder.append("[");
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue());
            builder.append("]");
        }
        builder.append("]");
        return builder.toString();
    }
}
