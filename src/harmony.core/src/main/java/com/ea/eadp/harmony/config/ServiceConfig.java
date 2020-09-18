/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import java.util.List;

/**
 * Created by leilin on 10/16/2014.
 */
public interface ServiceConfig {
    // get service name
    String getService();

    // get current node name
    String getNode();

    // get names of all serviceNodes
    List<String> getAllNodes();

    // get endpoint for remote command
    String getCommandEndpoint();
}
