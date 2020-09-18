/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.commands;

import com.caucho.hessian.client.HessianProxyFactory;
import com.ea.eadp.harmony.transition.TransitionConductor;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by VincentZhang on 5/24/2018.
 */
@Component
public class HessianFactory {
    private static Logger log = LoggerFactory.getLogger(HessianFactory.class);

    @Value("${harmony.server.scheme}")
    private String harmonyScheme;

    String endPointTemplate = "${scheme}://${hostname}:${port}/${function}";

    public TransitionConductor transitionConductor(String hostname, String harmonyServerPort) throws MalformedURLException {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("scheme", harmonyScheme);
        valuesMap.put("hostname", hostname);
        valuesMap.put("port", harmonyServerPort);
        valuesMap.put("function", "TransitionConductor");
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String endPointURL = sub.replace(endPointTemplate);
        log.info("Getting Hessian object from:" + endPointURL);
        HessianProxyFactory factory = new HessianProxyFactory();
        return (TransitionConductor)factory.create(TransitionConductor.class, endPointURL);
    }
}
