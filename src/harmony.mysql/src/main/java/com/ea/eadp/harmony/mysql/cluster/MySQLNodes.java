/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

import java.util.Map;

/**
 * Created by VincentZhang on 5/9/2018.
 */
public class MySQLNodes{
    public Map<String, MySQLNode> nodes;

    public Map<String, MySQLNode> getNodes(){
        return nodes;
    }
}
