/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.cluster;

/**
 * Created by VincentZhang on 5/10/2018.
 */
public class RedisProperties {
    public String repl_backlog_first_byte_offset;
    public String role;
    public String repl_backlog_size;
    public String master_sync_in_progress;
    public String connected_slaves;
    public String slave_priority;
    public String master_host;
    public String repl_backlog_histlen;
    public String slave_repl_offset;
    public String slave_read_only;
    public String master_link_status;
    public String repl_backlog_active;
    public String master_port;
    public String master_repl_offset;
    public String master_last_io_seconds_ago;

    public String getRepl_backlog_first_byte_offset() {
        return repl_backlog_first_byte_offset;
    }

    public String getRole() {
        return role;
    }

    public String getRepl_backlog_size() {
        return repl_backlog_size;
    }

    public String getMaster_sync_in_progress() {
        return master_sync_in_progress;
    }

    public String getConnected_slaves() {
        return connected_slaves;
    }

    public String getSlave_priority() {
        return slave_priority;
    }

    public String getMaster_host() {
        return master_host;
    }

    public String getRepl_backlog_histlen() {
        return repl_backlog_histlen;
    }

    public String getSlave_repl_offset() {
        return slave_repl_offset;
    }

    public String getSlave_read_only() {
        return slave_read_only;
    }

    public String getMaster_link_status() {
        return master_link_status;
    }

    public String getRepl_backlog_active() {
        return repl_backlog_active;
    }

    public String getMaster_port() {
        return master_port;
    }

    public String getMaster_repl_offset() {
        return master_repl_offset;
    }

    public String getMaster_last_io_seconds_ago() {
        return master_last_io_seconds_ago;
    }
}
