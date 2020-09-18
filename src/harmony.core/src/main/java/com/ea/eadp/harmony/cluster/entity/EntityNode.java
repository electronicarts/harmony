/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster.entity;

import com.ea.eadp.harmony.control.ServiceNodeMarker;

/**
 * Created by VincentZhang on 5/10/2018.
 */
public class EntityNode {
    public String status;
    public String marker;
    public String markerStep;

    public String getStatus() {
        return status;
    }

    public String getMarker() {
        return marker;
    }

    public String getDetail(){
        return ServiceNodeMarker.valueOf(marker).getDetail();
    }

    public String getAction(){
        return ServiceNodeMarker.valueOf(marker).getAction();
    }

    public String getMarkerStep() {
        return markerStep;
    }
}
