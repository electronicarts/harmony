/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.ea.eadp.harmony.shared.event.BeforeApplicationStartedEvent;
import com.ea.eadp.harmony.shared.event.EventHandler;
import com.ea.eadp.harmony.shared.event.EventHandlerClass;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Created by leilin on 10/15/2014.
 */
@EventHandlerClass
@Component
public class ServiceConfigRepositoryImpl implements ServiceConfigRepository {
    @Value("${service.list}")
    private String[] serviceList;

    @Value("${harmony.inspectorCount}")
    private long inspectorCount;

    @Autowired
    private HarmonyEnvironment environment;

    @Autowired
    private ServiceConfigFactory configFactory;

    private Map<String, ServiceClusterInfo> configMap = new HashMap<String, ServiceClusterInfo>();

    @Override
    public ServiceEnvironment getServiceEnvironment(String service, String node) {
        ServiceEnvironment target = new ServiceEnvironment(environment, service);
        target.setNode(node);
        return target;
    }

    @Override
    public List<String> getServiceList() {
        return Collections.unmodifiableList(Arrays.asList(serviceList));
    }

    @Override
    public long getInspectorCount() {
        return inspectorCount;
    }

    @Override
    public String getCurrentNode() {
        return environment.getNode();
    }

    @Override
    public List<String> getServiceNodes(String service) {
        return Collections.unmodifiableList(getServiceConfigEntry(service).getNodeList());
    }

    @Override
    public BaseServiceConfig getServiceConfig(String service) {
        return getServiceConfig(service, getCurrentNode());
    }

    @Override
    public BaseServiceConfig getServiceConfig(String service, String node) {
        BaseServiceConfig config = getServiceConfigEntry(service).getNodeConfigMap().get(node);
        if (config == null) {
            throw new IllegalArgumentException(String.format("service %s node %s doesn't exist", service, node));
        }

        return config;
    }

    @Override
    public BaseServiceConfig getServiceConfig(ServiceEnvironment environment) {
        return getServiceConfig(environment.getService(), environment.getNode());
    }

    private ServiceClusterInfo getServiceConfigEntry(String service) {
        ServiceClusterInfo entry = configMap.get(service);
        if (entry == null) {
            throw new IllegalArgumentException("service " + service + " doesn't exist");
        }

        return entry;
    }

    @Override
    public List<BaseServiceConfig> getInspectionTargets() {
        List<BaseServiceConfig> configs = new ArrayList<BaseServiceConfig>();

        // for each service
        for (String service : getServiceList()) {
            // for each node
            for (String node : getServiceNodes(service)) {
                // inspection target
                BaseServiceConfig config = getServiceConfig(service, node);
                configs.add(config);
            }
        }

        return configs;
    }

    @EventHandler(BeforeApplicationStartedEvent.class)
    public void onBeforeApplicationStarted(BeforeApplicationStartedEvent e) {
        reloadServiceConfig();
    }

    public void reloadServiceConfig() {
        configMap.clear();
        for (String service : serviceList) {
            ServiceEnvironment serviceEnvironment = new ServiceEnvironment(environment, service);
            BaseServiceConfig currentNodeConfig = configFactory.resolveServiceConfig(serviceEnvironment);

            HashMap<String, BaseServiceConfig> nodeConfigs = new HashMap<String, BaseServiceConfig>();

            for (String node : currentNodeConfig.getAllNodes()) {
                if (node.equals(environment.getNode())) {
                    nodeConfigs.put(node, currentNodeConfig);
                } else {
                    ServiceEnvironment nodeEnvironment = new ServiceEnvironment(serviceEnvironment);
                    nodeEnvironment.setNode(node);
                    BaseServiceConfig nodeConfig = configFactory.resolveServiceConfig(nodeEnvironment);
                    nodeConfigs.put(node, nodeConfig);
                }
            }

            ServiceClusterInfo info = new ServiceClusterInfo(service, currentNodeConfig.getAllNodes(), nodeConfigs);
            configMap.put(service, info);
        }
    }


    private static class ServiceClusterInfo {
        private String serviceName;
        private List<String> nodeList;
        private Map<String, BaseServiceConfig> nodeConfigMap;

        private ServiceClusterInfo(String serviceName, List<String> nodeList, Map<String, BaseServiceConfig> nodeConfigMap) {
            this.serviceName = serviceName;
            this.nodeList = nodeList;
            this.nodeConfigMap = nodeConfigMap;
        }

        public String getServiceName() {
            return serviceName;
        }

        public List<String> getNodeList() {
            return nodeList;
        }

        public Map<String, BaseServiceConfig> getNodeConfigMap() {
            return nodeConfigMap;
        }
    }
}
