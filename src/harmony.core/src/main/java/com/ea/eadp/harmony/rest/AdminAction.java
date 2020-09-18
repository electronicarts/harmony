/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest;

/**
 * Created by juding on 12/10/2014.
 */
public enum AdminAction {
    ACTIVATE_NODE, INACTIVATE_NODE,
    ONLINE_MASTER, OFFLINE_MASTER,
    ONLINE_SLAVE, OFFLINE_SLAVE,
    ASSIGN_ROLE, RESIGN_ROLE,
    FAILOVER_MASTER, FORCE_FAILOVER,
    ACCEPT_FACT, ENSURE_MASTER
}
