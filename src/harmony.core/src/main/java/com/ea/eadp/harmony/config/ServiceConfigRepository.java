/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;

import java.util.List;

/**
 * Created by leilin on 10/15/2014.
 */
public interface ServiceConfigRepository {
    // get service environment
    ServiceEnvironment getServiceEnvironment(String service, String node);

    // get all service names
    List<String> getServiceList();

    // get name of current node
    String getCurrentNode();

    // get all node names with given service
    List<String> getServiceNodes(String service);

    // get ServiceConfig for current node with given service
    BaseServiceConfig getServiceConfig(String service);

    // get ServiceConfig for given node with given service
    BaseServiceConfig getServiceConfig(String service, String node);

    // get ServiceConfig for given environment
    BaseServiceConfig getServiceConfig(ServiceEnvironment environment);

    // get all ServiceConfig for inspection targets
    List<BaseServiceConfig> getInspectionTargets();

    // get number of full Harmony processes
    long getInspectorCount();
}
