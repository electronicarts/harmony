/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.control;

/**
 * Created by juding on 7/12/17.
 */
public enum ServiceNodeMarker {
    GENERIC_INF_SVR() {
        public String getDetail() {
            return "Server is OK.";
        }

        public String getAction() {
            return "None.";
        }
    },
    GENERIC_ERR_SVR() {
        public String getDetail() {
            return "Server has issue.";
        }

        public String getAction() {
            return "Fix the issue.";
        }
    },
    // Redis related errors
    REDIS_VIP_WRONG() {
        public String getDetail() {
            return "Can't connect to Redis VIP!";
        }

        public String getAction() {
            return "Should perform failover to prevent data loss!";
        }
    },
    REDIS_ERR_AUTO_FAILOVER() {
        public String getDetail() {
            return "Redis auto failover is complete.";
        }

        public String getAction() {
            return "Check Redis cluster health and fix any issues found.";
        }
    },
    REDIS_ERR_PROCESS_RUNNING() {
        public String getDetail() {
            return "Redis process is not running.";
        }

        public String getAction() {
            return "Check Redis process running status, looks like it is not running now.";
        }
    },
    REDIS_ERR_REPLICATION() {
        public String getDetail() {
            return "Redis replication is not running.";
        }

        public String getAction() {
            return "Check Redis and ensure replication running status.";
        }
    },
    REDIS_ERR_MASTER_REPLICATIONMASTER() {
        public String getDetail() {
            return "Master node is not replication master.";
        }

        public String getAction() {
            return "Check Redis master node and ensure master node is really replication master " +
                    "to avoid data corruption.";
        }
    },
    REDIS_ERR_SLAVE_REPLICATIONSLAVE() {
        public String getDetail() {
            return "Slave node is running as replication master.";
        }

        public String getAction() {
            return "Check Redis slave node and ensure slave node is really replication slave " +
                    "to avoid data lost.";
        }
    },
    REDIS_ERR_REPLICATIONMASTER_NO_CLIENT() {
        public String getDetail() {
            return "Replication master has no slave connected.";
        }

        public String getAction() {
            return "Check Redis slave node and ensure slave is connected to master to avoid " +
                    "single point failure";
        }
    },
    REDIS_ERR_REPLICATIONSLAVE_MASTER_NOT_MATCH() {
        public String getDetail() {
            return "Replication master doesn't match configured master node.";
        }

        public String getAction() {
            return "Check which node this slave has connected and ensure slave is connected to " +
                    "the correct master to avoid data corruption.";
        }
    },
    REDIS_ERR_REPLICATIONSLAVE_MASTER_NOT_FOUND() {
        public String getDetail() {
            return "DNS failed when trying to find the master node.";
        }

        public String getAction() {
            return "Check DNS config and DNS server availability.";
        }
    },
    REDIS_ERR_REPLICATIONSLAVE_LINK_DOWN() {
        public String getDetail() {
            return "Replication link between master and slave down.";
        }

        public String getAction() {
            return "Check log to see if there's exist any replication related errors.";
        }
    },
    REDIS_ERR_REPLICATIONSLAVE_SLAVE_READ_ONLY() {
        public String getDetail() {
            return "Slave is writable.";
        }

        public String getAction() {
            return "Slave is writable, data might be corrupted.";
        }
    };

    public abstract String getDetail();

    public abstract String getAction();
}
