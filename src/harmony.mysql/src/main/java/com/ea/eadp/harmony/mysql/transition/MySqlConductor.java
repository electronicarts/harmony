/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.transition;

import com.ea.eadp.harmony.check.CheckResult;
import com.ea.eadp.harmony.check.NodeChecker;
import com.ea.eadp.harmony.check.ServiceNodeHealth;
import com.ea.eadp.harmony.cluster.ClusterManager;
import com.ea.eadp.harmony.command.CommandCenter;
import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.vip.VipAction;
import com.ea.eadp.harmony.command.vip.VipCommand;
import com.ea.eadp.harmony.config.BaseServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.entity.LinkLocation;
import com.ea.eadp.harmony.mysql.cluster.MySQLCluster;
import com.ea.eadp.harmony.mysql.cluster.MySQLClusterBuilder;
import com.ea.eadp.harmony.mysql.cluster.MySQLProperties;
import com.ea.eadp.harmony.mysql.command.MasterStatusResponse;
import com.ea.eadp.harmony.mysql.command.MySqlCommand;
import com.ea.eadp.harmony.mysql.command.MySqlLocationCommand;
import com.ea.eadp.harmony.mysql.command.StartSlaveCommand;
import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.entity.MasterLocation;
import com.ea.eadp.harmony.mysql.entity.MasterStatusDB;
import com.ea.eadp.harmony.mysql.entity.MySqlAction;
import com.ea.eadp.harmony.mysql.entity.StartSlaveUntil;
import com.ea.eadp.harmony.rest.AdminAction;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import com.ea.eadp.harmony.transition.TransitionConductor;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by juding on 10/24/2014.
 */
@Component
public class MySqlConductor extends ServiceSupport implements TransitionConductor {
    private final static Logger logger = LoggerFactory.getLogger(MySqlConductor.class);

    @Autowired
    MySQLClusterBuilder clusterBuilder;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private CommandCenter commandCenter;

    @Value("${adminApi.timeout.lock}")
    private int timeoutLock;

    private final static Map<ServiceNodeStatus, Set<AdminAction>> validCmds;

    static {
        Set<AdminAction> actions;
        validCmds = new HashMap<ServiceNodeStatus, Set<AdminAction>>();

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.ACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        validCmds.put(ServiceNodeStatus.INACTIVE, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.ONLINE_MASTER);
        actions.add(AdminAction.ONLINE_SLAVE);
        actions.add(AdminAction.INACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ASSIGN_ROLE);
        validCmds.put(ServiceNodeStatus.STANDBY, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.FAILOVER_MASTER);
        actions.add(AdminAction.FORCE_FAILOVER);
        actions.add(AdminAction.OFFLINE_MASTER);
        actions.add(AdminAction.OFFLINE_SLAVE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ASSIGN_ROLE);
        validCmds.put(ServiceNodeStatus.ONLINE, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.INACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ONLINE_SLAVE);
        validCmds.put(ServiceNodeStatus.DOWN, actions);
    }

    private void checkCondition(String service, String node, AdminAction action) {
        ServiceNodeStatus status = clusterManager.getServiceNodeStatus(service, node);
        if (!validCmds.get(status).contains(action))
            throw new RuntimeException("/" + service + "/" + node + " is " + status.name()
                    + ", therefore can't execute " + action.name());
    }

    private void handleFailure(Throwable t, boolean rethrow) {
        logger.error("Caught by safetyNet", t);
        if (rethrow)
            throw new RuntimeException(t);
    }

    private interface Thrower<TThr extends Throwable> {
        void execute() throws TThr;
    }

    private void conductTransition(String service, Thrower<Throwable> func) {
        InterProcessMutex mtx = null;
        try {
            String path = clusterManager.getLockPath(service);
            ZooKeeperService zkSvc = getZooKeeperService();
            mtx = zkSvc.createInterProcessMutex(path);
            if (mtx.acquire(timeoutLock, TimeUnit.MILLISECONDS)) {
                func.execute();
            } else {
                throw new RuntimeException("Unable to lock service");
            }
        } catch (Throwable t) {
            handleFailure(t, true);
        } finally {
            try {
                if (mtx != null)
                    mtx.release();
            } catch (Exception ex) {
                logger.error("Failed to release service mutex", ex);
            }
        }
    }

    @Override
    public void ensureMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                // checkCondition(service, node, AdminAction.ENSURE_MASTER);
                ensureMasterInternal(service, node);
            }
        });
    }

    private void ensureMasterInternal(String service, String node) throws Throwable {
        String currentMasterAlias = clusterManager.getCurrentMaster(service);

        String logDtl = "ensure service " + service
                + ", node " + node
                + ", current master " + currentMasterAlias;

        if (currentMasterAlias == null) {
            logger.warn("Null Master: " + logDtl);
            return;
        }

        ServiceEnvironment targetSvcEnv = getServiceEnvironmentForServiceNode(service, node);

        if (node.equals(currentMasterAlias)) {
            clearReadOnly(targetSvcEnv);
            addVip(targetSvcEnv, false, true);
            if (clusterManager.getCurrentPrimarySlave(service) == null) {
                addVip(targetSvcEnv, false, false);
            }
            logger.warn("Is Master: " + logDtl);
        } else {
            delVip(targetSvcEnv, false, true);
            killConnections(targetSvcEnv);
            setReadOnly(targetSvcEnv);
            logger.warn("Not Master: " + logDtl);
        }
    }

    @Override
    public void moveMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.FAILOVER_MASTER);
                moveMasterInternal(service, node);
            }
        });
    }

    @Override
    public void forceMoveMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.FORCE_FAILOVER);
                forceMoveMasterInternal(service, node);
            }
        });
    }

    private void moveMasterInternal(String service, String node) throws Throwable {
        logger.warn("Moving master now...");

        // Stage 1 - No changes: Get info

        String targetAlias = node;

        String currentMasterAlias = clusterManager.getCurrentMaster(service);
        logger.debug("Current master: " + currentMasterAlias);

        if (currentMasterAlias == null) {
            String errMsg = "Current master is not set";
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        if (currentMasterAlias.equals(targetAlias)) {
            logger.warn("Current master is the target");
            return;
        }
        ServiceNodeStatus currentMasterNodeStatus = getServiceNodeStatus(service, currentMasterAlias);
        if (currentMasterNodeStatus != ServiceNodeStatus.ONLINE)
            throw new RuntimeException(currentMasterAlias + " is not Online - " + currentMasterNodeStatus);
        ServiceEnvironment currentMasterSvcEnv = getServiceEnvironmentForServiceNode(service, currentMasterAlias);

        String primarySlaveAlias = clusterManager.getCurrentPrimarySlave(service);
        if (!primarySlaveAlias.equals(targetAlias)) {
            String errMsg = "Primary slave " + primarySlaveAlias + " is not target " + targetAlias;
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        logger.debug("Target node: " + targetAlias);
        String targetHost = getHostForServiceNode(service, targetAlias);
        ServiceEnvironment targetSvcEnv = getServiceEnvironmentForServiceNode(service, targetAlias);

        List<String> slaveAliases = getOtherOnlineSlaves(service, currentMasterAlias, targetAlias);
        logger.debug("Slave serviceNodes: " + slaveAliases);

        List<ServiceEnvironment> slaveSvcEnvs = new ArrayList<ServiceEnvironment>();
        for (String alias : slaveAliases) {
            ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, alias);
            slaveSvcEnvs.add(svcEnv);
        }

        List<ServiceEnvironment> allSlaveSvcEnvs = new ArrayList<ServiceEnvironment>();
        allSlaveSvcEnvs.add(targetSvcEnv);
        allSlaveSvcEnvs.addAll(slaveSvcEnvs);

        // Stage 2 - Minor changes: Sync slaves

        // Check Slave status

        MySQLCluster mysqlClusterInfo = (MySQLCluster) clusterBuilder.buildCluster(clusterManager.getClusterPath());
        MySQLProperties slaveProperties = mysqlClusterInfo.serviceNodes.get(service).nodes.get(targetAlias).properties;
        if (!("Yes".equals(slaveProperties.getSlaveIoRunning())) ||
                !("Yes".equals(slaveProperties.getSlaveSqlRunning()))) {
            throw new RuntimeException("Slave replication thread error! If failover now, data will unsync!");
        }

        MasterStatusDB currentMasterStatus2 = null;
        try {
            /*
            for (ServiceEnvironment actr : allSlaveSvcEnvs) {
                stopSlave(actr);
            }
            */

            MasterStatusDB currentMasterStatus = getMasterStatus(currentMasterSvcEnv);
            logger.info("currentMasterStatus = " + currentMasterStatus);

            /*
            for (ServiceEnvironment actr : allSlaveSvcEnvs) {
                startSlave(actr, StartSlaveUntil.FOR_MASTER, currentMasterStatus);
            }

            for (ServiceEnvironment actr : allSlaveSvcEnvs) {
                syncSlave(actr, currentMasterStatus);
                stopSlave(actr);
            }

            targetMasterStatus = getMasterStatus(targetSvcEnv);
            logger.info("targetMasterStatus = " + targetMasterStatus);
            */

            /*
            startSlave(targetSvcEnv);
            */

            long syncFastNano = getTimeoutSyncForService(service) * 1000000L;
            boolean syncIsFast = false;
            long syncRemain = 3;
            while (!syncIsFast && syncRemain > 0) {
                currentMasterStatus2 = getMasterStatus(currentMasterSvcEnv);
                long startTime = System.nanoTime();
                syncSlave(targetSvcEnv, currentMasterStatus2);
                long elapsedTime = System.nanoTime() - startTime;
                if (elapsedTime <= syncFastNano)
                    syncIsFast = true;
                syncRemain -= 1;
            }
            if (!syncIsFast)
                throw new RuntimeException("syncSlave took more than " + syncFastNano + " nano seconds");
        } catch (Throwable t) {
            /*
            bounceSlaves(allSlaveSvcEnvs);
            */
            throw t;
        }

        // Stage 3 - Major changes: Master transition

        MasterStatusDB targetMasterStatus2 = null;
        int highWaterMark = 0;
        try {
            delVip(currentMasterSvcEnv);
            highWaterMark++;
            killConnections(currentMasterSvcEnv);
            setReadOnly(currentMasterSvcEnv);
            highWaterMark++;
            currentMasterStatus2 = getMasterStatus(currentMasterSvcEnv);

            syncSlave(targetSvcEnv, currentMasterStatus2);

            targetMasterStatus2 = getMasterStatus(targetSvcEnv);
            clearReadOnly(targetSvcEnv);
            highWaterMark++;
            killWarmUp(targetSvcEnv);
            addVip(targetSvcEnv);
            highWaterMark++;
        } catch (Throwable t) {
            int rollWaterMark = 3;
            try {
                if (highWaterMark >= rollWaterMark) {
                    delVip(targetSvcEnv);
                }
                rollWaterMark--;
                if (highWaterMark >= rollWaterMark) {
                    setReadOnly(targetSvcEnv);
                }
                rollWaterMark--;
                if (highWaterMark >= rollWaterMark) {
                    clearReadOnly(currentMasterSvcEnv);
                }
                rollWaterMark--;
                if (highWaterMark >= rollWaterMark) {
                    addVip(currentMasterSvcEnv);
                }
                rollWaterMark--;
            } catch (Throwable f) {
                String errMsg = String.format("Transition failed. Rollback failed. WaterMark = %d, %d", highWaterMark, rollWaterMark);
                logger.error(errMsg);
                throw new Throwable(errMsg, t);
            } finally {
                /*
                bounceSlaves(slaveSvcEnvs);
                */
            }
            String errMsg = String.format("Transition failed. Rollback successful. WaterMark = %d, %d", highWaterMark, rollWaterMark);
            logger.error(errMsg);
            throw new Throwable(errMsg, t);
        }

        logger.info("currentMasterStatus2 = " + currentMasterStatus2);
        logger.info("targetMasterStatus2 = " + targetMasterStatus2);

        // Stage 4 - Minor changes: Adjust slaves

        /*
        stopSlave(targetSvcEnv);
        resetSlaveAll(targetSvcEnv);

        changeMaster(currentMasterSvcEnv,
                new MasterLocation(targetHost, targetMasterStatus2.file, targetMasterStatus2.position));
        startSlave(currentMasterSvcEnv);
        */
        clusterManager.setCurrentPrimarySlave(service, currentMasterAlias);

        for (ServiceEnvironment actr : slaveSvcEnvs) {
            /*
            changeMaster(actr,
                    new MasterLocation(targetHost, targetMasterStatus.getFile(), targetMasterStatus.getPosition()));
            */
            /*
            startSlave(actr);
            */
        }

        logger.warn("Moving master completed.");
    }

    private MasterStatusDB forceMoveMasterInternal(String service, String node) throws Throwable {
        logger.warn("Force moving master now...");

        String targetAlias = node;

        String currentMasterAlias = clusterManager.getCurrentMaster(service);
        logger.debug("Current master: " + currentMasterAlias);

        if (currentMasterAlias == null) {
            String errMsg = "Current master is not set";
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        if (currentMasterAlias.equals(targetAlias)) {
            logger.warn("Current master is the target");
            return null;
        }
        ServiceNodeStatus currentMasterNodeStatus = getServiceNodeStatus(service, currentMasterAlias);
        if (currentMasterNodeStatus != ServiceNodeStatus.ONLINE &&
                currentMasterNodeStatus != ServiceNodeStatus.DOWN)
            throw new RuntimeException(currentMasterAlias + " is not Online or Down - " + currentMasterNodeStatus);
        ServiceEnvironment currentMasterSvcEnv = getServiceEnvironmentForServiceNode(service, currentMasterAlias);

        String primarySlaveAlias = clusterManager.getCurrentPrimarySlave(service);
        if (!primarySlaveAlias.equals(targetAlias)) {
            String errMsg = "Primary slave " + primarySlaveAlias + " is not target " + targetAlias;
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }

// During force failover, this node might not be able to connect to master ,so skip this check.
//        MySQLCluster mysqlClusterInfo = (MySQLCluster) clusterBuilder.buildCluster(clusterManager.getClusterPath());
//        MySQLProperties slaveProperties = mysqlClusterInfo.serviceNodes.get(service).nodes.get(targetAlias).properties;
//        if(!("Yes".equals(slaveProperties.getSlaveIoRunning()))||
//                !("Yes".equals(slaveProperties.getSlaveSqlRunning()))){
//            throw new RuntimeException("Slave replication thread error! If failover now, data will unsync!");
//        }

        logger.debug("Target node: " + targetAlias);
        String targetHost = getHostForServiceNode(service, targetAlias);
        ServiceEnvironment targetSvcEnv = getServiceEnvironmentForServiceNode(service, targetAlias);

        try {
            delVip(currentMasterSvcEnv);
        } catch (Throwable t) {
            handleFailure(t, false);
        }

        try {
            killConnections(currentMasterSvcEnv);
        } catch (Throwable t) {
            handleFailure(t, false);
        }

        try {
            setReadOnly(currentMasterSvcEnv);
        } catch (Throwable t) {
            handleFailure(t, false);
        }

        /*
        stopSlave(targetSvcEnv);
        resetSlaveAll(targetSvcEnv);
        */
        if (primarySlaveAlias.equals(targetAlias)) {
            clusterManager.setCurrentPrimarySlave(service, currentMasterAlias);
        }

        MasterStatusDB targetMasterStatus2 = getMasterStatus(targetSvcEnv);
        logger.info("targetMasterStatus2 = " + targetMasterStatus2);

        clearReadOnly(targetSvcEnv);
        killWarmUp(targetSvcEnv);
        addVip(targetSvcEnv);

        logger.warn("Force moving master completed.");

        return targetMasterStatus2;
    }

    @Override
    public void moveReader(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                moveReaderInternal(service, node);
            }
        });
    }

    private void moveReaderInternal(String service, String node) throws Throwable {
        logger.warn("Moving read vip now...");

        String targetAlias = node;
        if (clusterManager.getNodeReaderVipStatus(service, targetAlias)) {
            logger.warn("Current VIP holder is the target");
            return;
        }

        List<String> currentNodeAlias = new LinkedList<>();

        currentNodeAlias.add(clusterManager.getCurrentMaster(service));
        currentNodeAlias.add(clusterManager.getCurrentPrimarySlave(service));

        ServiceNodeStatus targetNodeStatus = getServiceNodeStatus(service, targetAlias);
        if (targetNodeStatus != ServiceNodeStatus.ONLINE) {
            throw new RuntimeException("Target node is not Online - " + targetNodeStatus);
        }

        for (String nodeAlias : currentNodeAlias) {
            if (!nodeAlias.equals(targetAlias)) {
                ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, nodeAlias);
                delVip(svcEnv, false, false);
            }
        }

        ServiceEnvironment targetSvcEnv = getServiceEnvironmentForServiceNode(service, targetAlias);
        addVip(targetSvcEnv, false, false);

        logger.warn("Moving read vip completed");
    }

    private long getTimeoutSyncForService(String service) {
        ServiceConfig config = serviceConfigRepository.getServiceConfig(service);
        return ((MySqlServiceConfig) config).getTimeoutSync();
    }

    private String getHostForServiceNode(String service, String node) {
        ServiceConfig config = serviceConfigRepository.getServiceConfig(service, node);
        return ((BaseServiceConfig) config).getHost();
    }

    private ServiceEnvironment getServiceEnvironmentForServiceNode(String service, String node) {
        return serviceConfigRepository.getServiceEnvironment(service, node);
    }

    private ServiceNodeStatus getServiceNodeStatus(String service, String node) {
        return clusterManager.getServiceNodeStatus(service, node);
    }

    private List<String> getOtherOnlineSlaves(String service, String masterNode, String targetNode) {
        List<String> nodeList = new ArrayList<String>();
        List<String> serviceNodes = serviceConfigRepository.getServiceNodes(service);
        for (String node : serviceNodes) {
            ServiceNodeStatus status = getServiceNodeStatus(service, node);
            switch (status) {
                case DOWN:
                    throw new RuntimeException("/" + service + "/" + node + " is " + status);
                case ONLINE:
                    if (!node.equals(masterNode) && !node.equals(targetNode))
                        nodeList.add(node);
                    break;
            }
        }
        return nodeList;
    }

    private void bounceSlaves(List<ServiceEnvironment> slaveList) {
        for (ServiceEnvironment svcEnv : slaveList) {
            try {
                stopSlave(svcEnv);
                startSlave(svcEnv);
                logger.info("Bouncing slave " + svcEnv + " successful.");
            } catch (Throwable t) {
                logger.error("Bouncing slave " + svcEnv + " failed.\n" + t.getMessage());
            }
        }
    }

    private void addVip(ServiceEnvironment serviceEnvironment) {
        addVip(serviceEnvironment, true, true);
    }

    private void addVip(ServiceEnvironment serviceEnvironment, boolean updateMaster, boolean isWriter) {
        HarmonyCommand command = new VipCommand(serviceEnvironment, isWriter ? VipAction.ADD_VIP_IPT : VipAction.ADD_VIP_READ_IPT);
        executeCommand(command);
        if (updateMaster)
            clusterManager.setCurrentMaster(serviceEnvironment.getService(), serviceEnvironment.getNode());
    }

    private void delVip(ServiceEnvironment serviceEnvironment) {
        delVip(serviceEnvironment, true, true);
    }

    private void delVip(ServiceEnvironment serviceEnvironment, boolean updateMaster, boolean isWriter) {
        HarmonyCommand command = new VipCommand(serviceEnvironment, isWriter ? VipAction.DEL_VIP_IPT : VipAction.DEL_VIP_READ_IPT);
        executeCommand(command);
        if (updateMaster)
            clusterManager.setCurrentMaster(serviceEnvironment.getService(), null);
    }

    private void setReadOnly(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.SET_READ_ONLY);
        executeCommand(command);
    }

    private void clearReadOnly(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.CLEAR_READ_ONLY);
        executeCommand(command);
    }

    private MasterStatusDB getMasterStatus(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.GET_MASTER_STATUS);
        HarmonyCommandResult result = executeCommand(command);
        MasterStatusDB masterStatusDB = ((MasterStatusResponse) result).getStatus();
        return masterStatusDB;
    }

    private void startSlave(ServiceEnvironment serviceEnvironment) {
        startSlave(serviceEnvironment, StartSlaveUntil.FOR_EVER, null);
    }

    private void startSlave(ServiceEnvironment serviceEnvironment, StartSlaveUntil untilType, MasterStatusDB masterStatusDB) {
        MasterLocation masterLocation = (masterStatusDB == null) ? null :
                new MasterLocation(null, masterStatusDB.getFile(), masterStatusDB.getPosition());
        HarmonyCommand command = new StartSlaveCommand(serviceEnvironment, MySqlAction.START_SLAVE, masterLocation, untilType);
        executeCommand(command);
    }

    private void syncSlave(ServiceEnvironment serviceEnvironment, MasterStatusDB masterStatusDB) {
        MasterLocation masterLocation =
                new MasterLocation(null, masterStatusDB.getFile(), masterStatusDB.getPosition());
        HarmonyCommand command = new MySqlLocationCommand(serviceEnvironment, MySqlAction.WAIT_MASTER_POS, masterLocation);
        executeCommand(command);
    }

    private void stopSlave(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.STOP_SLAVE);
        executeCommand(command);
    }

    private void resetSlaveAll(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.RESET_SLAVE_ALL);
        executeCommand(command);
    }

    private void killConnections(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.KILL_CONNECTIONS);
        executeCommand(command);
    }

    private void killWarmUp(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.KILL_WARMUP);
        executeCommand(command);
    }

    private HarmonyCommandResult executeCommand(HarmonyCommand command) {
        HarmonyCommandResult result = commandCenter.executeCommand(command);
        logger.info("MySqlConductor.executeCommand: " + command + " => " + result);
        return result;
    }

    @Autowired
    private NodeChecker nodeChecker;

    @Override
    public void activateServer(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.ACTIVATE_NODE);
                activateServerInternal(service, node);
            }
        });
    }

    @Override
    public void inactivateServer(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.INACTIVATE_NODE);
                inactivateServerInternal(service, node);
            }
        });
    }

    @Override
    public void onlineMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.ONLINE_MASTER);
                onlineMasterInternal(service, node);
            }
        });
    }

    @Override
    public void offlineMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.OFFLINE_MASTER);
                offlineMasterInternal(service, node);
            }
        });
    }

    @Override
    public void onlineSlave(final String service, final String node, final LinkLocation location) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.ONLINE_SLAVE);
                onlineSlaveInternal(service, node, location);
            }
        });
    }

    @Override
    public void offlineSlave(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.OFFLINE_SLAVE);
                offlineSlaveInternal(service, node);
            }
        });
    }

    @Override
    public void assignRole(final String service, final String node, final String role, final LinkLocation location) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.ASSIGN_ROLE);
                assignRoleInternal(service, node, role, location);
            }
        });
    }

    @Override
    public void resignRole(final String service, final String node, final String role) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkCondition(service, node, AdminAction.RESIGN_ROLE);
                resignRoleInternal(service, node, role);
            }
        });
    }

    private void activateServerInternal(String service, String node) throws Throwable {
        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        restartMySql(svcEnv);

        ServiceConfig config = serviceConfigRepository.getServiceConfig(service, node);
        CheckResult checkResult = nodeChecker.checkNode(config);

        if (checkResult.getHealth() == ServiceNodeHealth.HEALTH_OK) {
            setServerStatus(service, node, ServiceNodeStatus.STANDBY);
        }
    }

    private void inactivateServerInternal(String service, String node) throws Throwable {
        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        stopMySql(svcEnv);

        setServerStatus(service, node, ServiceNodeStatus.INACTIVE);
    }

    private void onlineMasterInternal(String service, String node) throws Throwable {
        if (clusterManager.getCurrentMaster(service) != null) {
            throw new RuntimeException("Current master already exists");
        }

        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        addVip(svcEnv);
        clearReadOnly(svcEnv);

        if (clusterManager.getCurrentPrimarySlave(service) == null) {
            addVip(svcEnv, false, true);
        }

        setServerStatus(service, node, ServiceNodeStatus.ONLINE);
    }

    private void offlineMasterInternal(String service, String node) throws Throwable {
        if (clusterManager.getCurrentPrimarySlave(service) != null) {
            throw new RuntimeException("Primary slave already exists");
        }

        if (!clusterManager.getCurrentMaster(service).equals(node)) {
            throw new RuntimeException("Current master is not " + node);
        }

        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        setReadOnly(svcEnv);
        delVip(svcEnv);

        setServerStatus(service, node, ServiceNodeStatus.STANDBY);
    }

    private void onlineSlaveInternal(String service, String node, LinkLocation location) throws Throwable {
        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        onlineSlave(svcEnv, (MasterLocation) location);

        setServerStatus(service, node, ServiceNodeStatus.ONLINE);
    }

    private void offlineSlaveInternal(String service, String node) throws Throwable {
        if (clusterManager.getCurrentMaster(service).equals(node)) {
            throw new RuntimeException("Current master is " + node);
        }

        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, node);
        stopSlave(svcEnv);

        setServerStatus(service, node, ServiceNodeStatus.STANDBY);
    }

    private void assignRoleInternal(String service, String node, String role, LinkLocation location) throws Throwable {
        if (clusterManager.getCurrentPrimarySlave(service) != null) {
            throw new RuntimeException("Primary slave already exists");
        }

        String currMaster = clusterManager.getCurrentMaster(service);
        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, currMaster);
        assignPrimary(svcEnv, (MasterLocation) location);

        clusterManager.setCurrentPrimarySlave(service, node);
    }

    private void resignRoleInternal(String service, String node, String role) throws Throwable {
        if (!clusterManager.getCurrentPrimarySlave(service).equals(node)) {
            throw new RuntimeException("Primary slave is not " + node);
        }

        String currMaster = clusterManager.getCurrentMaster(service);
        ServiceEnvironment svcEnv = getServiceEnvironmentForServiceNode(service, currMaster);
        stopSlave(svcEnv);

        clusterManager.setCurrentPrimarySlave(service, null);
    }

    @Override
    public void setServerStatus(String service, String node, ServiceNodeStatus status) {
        ZooKeeperService zkSvc = getZooKeeperService();
        String path = clusterManager.getStatusPath(service, node);
        zkSvc.setNodeStringData(path, status.name());
    }

    private void restartMySql(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.RESTART);
        executeCommand(command);
    }

    private void stopMySql(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new MySqlCommand(serviceEnvironment, MySqlAction.STOP);
        executeCommand(command);
    }

    private void onlineSlave(ServiceEnvironment serviceEnvironment, MasterLocation masterLocation) {
        HarmonyCommand command = new MySqlLocationCommand(serviceEnvironment, MySqlAction.ONLINE_SLAVE, masterLocation);
        executeCommand(command);
    }

    private void assignPrimary(ServiceEnvironment serviceEnvironment, MasterLocation masterLocation) {
        HarmonyCommand command = new MySqlLocationCommand(serviceEnvironment, MySqlAction.ASSIGN_PRIMARY, masterLocation);
        executeCommand(command);
    }
}
