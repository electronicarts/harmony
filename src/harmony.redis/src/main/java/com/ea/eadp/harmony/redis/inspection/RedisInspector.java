/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.inspection;

import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.command.ResultType;
import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeMarker;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.inspection.InspectionResult;
import com.ea.eadp.harmony.inspection.NodeInspector;
import com.ea.eadp.harmony.redis.command.RedisReplicationCommandResult;
import com.ea.eadp.harmony.redis.config.RedisServiceConfig;
import com.ea.eadp.harmony.redis.transition.RedisConductor;
import com.ea.eadp.harmony.redis.utils.RedisCommandWrapper;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.email.EmailService;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import com.ea.eadp.harmony.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Created by leilin on 10/16/2014.
 */
@Component
public class RedisInspector extends ServiceSupport implements NodeInspector<RedisServiceConfig> {
    private final static Logger logger = LoggerFactory.getLogger(RedisInspector.class);

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private RedisConductor redisConductor;

    @Override
    public InspectionResult inspectNode(RedisServiceConfig config) {
        logger.info("Inspecting Redis service /{}/{} {}:{}",
                new Object[]{config.getService(), config.getNode(), config.getHost(), config.getPort()});

        String service = config.getService();
        String currMaster = clusterManager.getCurrentMaster(service);

        // Not sure why do we need to check this first????
        if (currMaster == null) {
            logger.warn("Cannot inspect Redis service /{}/{} - master not found",
                    new Object[]{config.getService(), config.getNode()});
            return null;
        }

        // Inspect master status through real IP address
        ServiceNodeMarker marker = ServiceNodeMarker.REDIS_ERR_PROCESS_RUNNING;
        RedisCommandWrapper inspectCommand = new RedisCommandWrapper(config);
        ServiceNodeStatus nodeStatus = inspectCommand.CheckRedisHealth();

        boolean everyThingGoodSofar = false;
        // If node is down, no need to perform more inspections
        if (nodeStatus.equals(ServiceNodeStatus.ONLINE)) {
            logger.info(config.getService() + " " + config.getNode() + " master status : " + nodeStatus.toString());

            marker = ServiceNodeMarker.REDIS_ERR_REPLICATION;
            RedisReplicationCommandResult res = inspectCommand.checkRedisReplication();
            if (res.getResultType() == ResultType.SUCCEEDED) {
                if (res.getRedisReplicationStatusEntity() != null) {
                    logger.info(config.getService() + " " + config.getNode() + " replication status: " + res.getRedisReplicationStatusEntity().toString());
                    RedisReplicationStatusEntity replicationStatusEntity = res.getRedisReplicationStatusEntity();
                    String replicationRole = replicationStatusEntity.getRedisReplicationProperty("role");
                    writeReplicationStatus(config.getService(), config.getNode(), replicationStatusEntity);

                    // Chain, chain, chain
                    if (currMaster.equals(config.getNode())) { // Current node is master node
                        marker = ServiceNodeMarker.REDIS_ERR_MASTER_REPLICATIONMASTER;
                        if (replicationRole.equals("master")) {
                            marker = ServiceNodeMarker.GENERIC_INF_SVR;
                        }
                    } else {
                        marker = ServiceNodeMarker.REDIS_ERR_SLAVE_REPLICATIONSLAVE;
                        if (replicationRole.equals("slave")) {
                            marker = ServiceNodeMarker.GENERIC_INF_SVR;
                        }
                    }

                    if (marker == ServiceNodeMarker.GENERIC_INF_SVR) {
                        if (replicationRole.equals("master")) {
                            String connectedSlaves = replicationStatusEntity.getRedisReplicationProperty("connected_slaves");
                            marker = ServiceNodeMarker.REDIS_ERR_REPLICATIONMASTER_NO_CLIENT;
                            if (Integer.valueOf(connectedSlaves) > 0) {
                                everyThingGoodSofar = true;
                            }
                        } else if (replicationRole.equals("slave")) {
                            BaseServiceConfig masterNodeConfig = serviceConfigRepository.
                                    getServiceConfig(config.getService(), currMaster);
                            try {
                                marker = ServiceNodeMarker.REDIS_ERR_REPLICATIONSLAVE_MASTER_NOT_MATCH;
                                Integer masterNodeHost = Helper.host2Long(masterNodeConfig.getHost());
                                String replicationMasterHostName = replicationStatusEntity.getRedisReplicationProperty("master_host");
                                Integer replicationMasterHost = Helper.host2Long(replicationMasterHostName);

                                Integer masterNodePort = masterNodeConfig.getPort();
                                Integer replicationMasterPort = Integer.valueOf(
                                        replicationStatusEntity.getRedisReplicationProperty("master_port"));

                                if (masterNodeHost.equals(replicationMasterHost) && masterNodePort.equals(replicationMasterPort)) {
                                    marker = ServiceNodeMarker.REDIS_ERR_REPLICATIONSLAVE_LINK_DOWN;
                                    String link_status = replicationStatusEntity.getRedisReplicationProperty("master_link_status");
                                    if (link_status.equals("up")) {
                                        marker = ServiceNodeMarker.REDIS_ERR_REPLICATIONSLAVE_SLAVE_READ_ONLY;
                                        String slave_read_only = replicationStatusEntity.getRedisReplicationProperty("slave_read_only");
                                        if (slave_read_only.equals("1")) {
//                                            String master_repl_offset = replicationStatusEntity.getRedisReplicationProperty("master_repl_offset");
//                                            if (Integer.valueOf(master_repl_offset) < 10) {

                                            everyThingGoodSofar = true;
//                                            }

                                        }
                                    }
                                }
                            } catch (UnknownHostException e) {
                                marker = ServiceNodeMarker.REDIS_ERR_REPLICATIONSLAVE_MASTER_NOT_FOUND;
                            }
                        }

                        // If this node is master, check it's VIP status.
                        // If something wrong with VIP, mark master node as down to force failover.
                        if (Objects.equals(currMaster, config.getNode())) {
                            marker = ServiceNodeMarker.REDIS_VIP_WRONG;
                            RedisCommandWrapper vipInspectCommand = new RedisCommandWrapper(config.getWriterVip(), config.getPort(),
                                    config.getRedisPassword(), config.getRedisIntallPath());
                            nodeStatus = vipInspectCommand.CheckRedisHealth();
                            logger.info(config.getService() + " " + config.getNode() + " VIP status : " + nodeStatus.toString());
                            if (nodeStatus.equals(ServiceNodeStatus.DOWN)) {
                                // redisConductor.ensureMaster(service, config.getNode());
                                everyThingGoodSofar = false;
                            }
                        }

                        if (everyThingGoodSofar == true) {
                            marker = ServiceNodeMarker.GENERIC_INF_SVR;
                        }
                    }

                }
            }
        }

        // Write marker into zookeeper
        setServiceNodeMarker(config.getService(), config.getNode(), marker);

        InspectionResult result = new InspectionResult(nodeStatus);
        return result;
    }


    private void setServiceNodeMarker(String service, String node, ServiceNodeMarker marker) {
        String atPath = clusterManager.getMarkerPath(service, node);
        ZooKeeperService zkSvc = getZooKeeperService();
        zkSvc.setNodeStringData(atPath, marker.name());
    }

    private void writeReplicationStatus(String service, String node, RedisReplicationStatusEntity replicationStatusEntity) {
        String propertyPath = clusterManager.getPropertiesPath(service, node);
        for (String key : replicationStatusEntity.getProperties().keySet()) {
            String value = replicationStatusEntity.getProperties().get(key);
            ZooKeeperService zkSvc = getZooKeeperService();
            String zkprPath = propertyPath + "/" + key;
            zkSvc.ensurePath(zkprPath);
            zkSvc.setNodeStringData(propertyPath + "/" + key, value);
        }
    }
}
