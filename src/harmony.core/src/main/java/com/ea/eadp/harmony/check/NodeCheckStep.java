/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.check;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.shared.email.EmailCategory;
import com.ea.eadp.harmony.shared.email.EmailSender;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by VincentZhang on 5/11/2018.
 */
public abstract class NodeCheckStep implements CheckStep {
    @Value("${harmony.env.universe}")
    private String environment;

    @Autowired
    private EmailSender emailSender;
    // zk support
    @Autowired
    private ZooKeeperService zooKeeperService;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    public EmailSender getEmailSender() {
        return emailSender;
    }

    public ZooKeeperService getZooKeeperService() {
        return zooKeeperService;
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public ServiceConfigRepository getServiceConfigRepository() {
        return serviceConfigRepository;
    }

    private NodeCheckStep next;

    public abstract NodeCheckStepResult check(Map dataObjectMap);

    public abstract String rootCause();

    public abstract String action();

    public String getEnvironment(){
        return environment;
    }

    // Help function to get hostname from service and node.
    protected String getHostName(String service, String node) {
        ServiceConfig serviceConfig = getServiceConfigRepository().getServiceConfig(service, node);
        String hostName = null;
        if (serviceConfig instanceof BaseServiceConfig) {
            hostName = ((BaseServiceConfig) serviceConfig).getHost();
        }
        return hostName;
    }

    @Override
    public NodeCheckStepResult handle() {
        Map dataObjectMapping = new HashMap();

        NodeCheckStepResult checkStepResult = check(dataObjectMapping);
        if(checkStepResult != NodeCheckStepResult.SUCCEEDED) { // Any warning or error happened, send email.
            if (dataObjectMapping != null) {
                dataObjectMapping.put("environment", environment);
                dataObjectMapping.put("cluster", NodeCheckContext.get("cluster"));
                dataObjectMapping.put("root_cause", rootCause());
                dataObjectMapping.put("action", action());

                if(NodeCheckContext.get("marker") == null){ // Always only record the first warning occurred.
                    NodeCheckContext.put("marker", this);
                }
            }

            EmailCategory emailCategory = NodeCheckStepResult.ERROR.equals(checkStepResult)?
                    EmailCategory.ERROR:EmailCategory.WARN;

            emailSender.postEmail(emailCategory, this.getClass(),
                    getTemplateName(), "Root cause:" + rootCause(), dataObjectMapping);
        }

        if ( checkStepResult != NodeCheckStepResult.ERROR) { // If not error, continue
            if (next != null)
                return next.handle();
        }
        return checkStepResult;
    }

    public NodeCheckStep setNext(NodeCheckStep next) {
        this.next = next;
        return this.next;
    }
    protected abstract String getTemplateName();
}
