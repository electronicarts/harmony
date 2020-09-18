/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.transition;

import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.entity.LinkLocation;

/**
 * Created by juding on 10/24/2014.
 */
public interface TransitionConductor {
    void moveMaster(String service, String node);
    void forceMoveMaster(String service, String node);

    void moveReader(String service, String node);

    void ensureMaster(String service, String node);

    void setServerStatus(String service, String node, ServiceNodeStatus status);

    void activateServer(String service, String node);
    void inactivateServer(String service, String node);
    void onlineMaster(String service, String node);
    void offlineMaster(String service, String node);
    void onlineSlave(String service, String node, LinkLocation location);
    void offlineSlave(String service, String node);
    void assignRole(String service, String node, String role, LinkLocation location);
    void resignRole(String service, String node, String role);
}
