/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

import com.ea.eadp.harmony.cluster.entity.EntityNode;

/**
 * Created by VincentZhang on 3/27/2018.
 */
public class MySQLNode extends EntityNode{
    public MySQLProperties properties;

    public MySQLProperties getProperties(){
        return properties;
    }
}
