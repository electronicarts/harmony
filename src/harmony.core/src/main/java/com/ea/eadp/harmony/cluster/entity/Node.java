/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * User: leilin
 * Date: 10/2/14
 */
public class Node {
    @NotEmpty
    private String alias;

    @NotEmpty
    private String host;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
