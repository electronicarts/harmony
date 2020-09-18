/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest;

import com.ea.eadp.harmony.transition.TransitionConductor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by VincentZhang on 2/26/2018.
 */
@RestController
public class ClusterController {
    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TransitionConductor transitionConductor;

    @Bean(name="/EntityClusterService")
    public HessianServiceExporter clusterControllerService(){
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setService(clusterService);
        exporter.setServiceInterface(ClusterService.class);
        return exporter;
    }

    @Bean(name="/TransitionConductor")
    public HessianServiceExporter transitionConductor(){
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setService(transitionConductor);
        exporter.setServiceInterface(TransitionConductor.class);
        return exporter;
    }
}
