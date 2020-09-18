/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.entity;

/**
 * Created by juding on 10/29/2014.
 */
public enum MySqlAction {
    START, RESTART, STOP,
    CLEAR_READ_ONLY, SET_READ_ONLY,
    GET_MASTER_STATUS,
    WAIT_MASTER_POS, START_SLAVE, STOP_SLAVE, RESET_SLAVE_ALL,
    KILL_CONNECTIONS,
    ONLINE_SLAVE, ASSIGN_PRIMARY,
    KILL_WARMUP
}
