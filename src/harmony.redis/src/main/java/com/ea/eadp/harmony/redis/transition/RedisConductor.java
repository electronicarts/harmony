/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.transition;

import com.ea.eadp.harmony.command.CommandCenter;
import com.ea.eadp.harmony.command.HarmonyCommand;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.command.ResultType;
import com.ea.eadp.harmony.command.vip.VipAction;
import com.ea.eadp.harmony.command.vip.VipCommand;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.control.ServiceNodeStatus;
import com.ea.eadp.harmony.entity.LinkLocation;
import com.ea.eadp.harmony.redis.command.RedisCommand;
import com.ea.eadp.harmony.redis.command.RedisFailoverCommand;
import com.ea.eadp.harmony.redis.command.RedisReplicationCommandResult;
import com.ea.eadp.harmony.redis.entity.RedisAction;
import com.ea.eadp.harmony.rest.AdminAction;
import com.ea.eadp.harmony.service.ServiceSupport;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.ea.eadp.harmony.transition.TransitionConductor;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ea.eadp.harmony.shared.zookeeper.ZooKeeperService;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by juding on 5/18/16.
 */
@Component
public class RedisConductor extends ServiceSupport implements TransitionConductor {
    private final static Logger logger = LoggerFactory.getLogger(RedisConductor.class);

    @Autowired
    private com.ea.eadp.harmony.cluster.ClusterManager clusterManager;

    @Autowired
    private CommandCenter commandCenter;

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Value("${adminApi.timeout.lock}")
    private int lockTimeout;


    private final static Map<ServiceNodeStatus, Set<AdminAction>> validCommands;
    static {
        Set<AdminAction> actions;
        validCommands = new HashMap<ServiceNodeStatus, Set<AdminAction>>();

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.ACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        validCommands.put(ServiceNodeStatus.INACTIVE, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.ONLINE_MASTER);
        actions.add(AdminAction.ONLINE_SLAVE);
        actions.add(AdminAction.INACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ASSIGN_ROLE);
        validCommands.put(ServiceNodeStatus.STANDBY, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.FAILOVER_MASTER);
        actions.add(AdminAction.FORCE_FAILOVER);
        actions.add(AdminAction.OFFLINE_MASTER);
        actions.add(AdminAction.OFFLINE_SLAVE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ASSIGN_ROLE);
        validCommands.put(ServiceNodeStatus.ONLINE, actions);

        actions = new HashSet<AdminAction>();
        actions.add(AdminAction.INACTIVATE_NODE);
        actions.add(AdminAction.RESIGN_ROLE);
        actions.add(AdminAction.ONLINE_SLAVE);
        validCommands.put(ServiceNodeStatus.DOWN, actions);
    }

    private void checkPreCondition(String service, String node, AdminAction action) {
        ServiceNodeStatus status = clusterManager.getServiceNodeStatus(service, node);
        if (! validCommands.get(status).contains(action))
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
            if (mtx.acquire(lockTimeout, TimeUnit.MILLISECONDS)) {
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

    private HarmonyCommandResult executeCommand(HarmonyCommand command) {
        HarmonyCommandResult result = commandCenter.executeCommand(command);
        logger.info("RedisConductor.executeCommand: " + command + " => " + result);
        return result;
    }

    private HarmonyCommandResult removeVIP(ServiceEnvironment serviceEnvironment, Boolean masterUpdated) {
        HarmonyCommand command = new VipCommand(serviceEnvironment, VipAction.DEL_VIP);
        HarmonyCommandResult res = executeCommand(command);
        if (masterUpdated) {
            clusterManager.setCurrentMaster(serviceEnvironment.getService(), null);
        }
        return res;
    }

    private HarmonyCommandResult placeVIP(ServiceEnvironment serviceEnvironment, Boolean masterUpdated) {
        HarmonyCommand command = new VipCommand(serviceEnvironment, VipAction.ADD_VIP);
        HarmonyCommandResult res = executeCommand(command);
        if (masterUpdated) {
            clusterManager.setCurrentMaster(serviceEnvironment.getService(), serviceEnvironment.getNode());
        }
        return res;
    }

    private HarmonyCommandResult killConnections(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new RedisCommand(serviceEnvironment, RedisAction.Kill_CONNECTIONS);
        return executeCommand(command);
    }

    private HarmonyCommandResult getRedisReplicationStatus(ServiceEnvironment serviceEnvironment) {
        HarmonyCommand command = new RedisCommand(serviceEnvironment, RedisAction.GET_REPLICATION_STATUS);
        return executeCommand(command);

    }

    private HarmonyCommandResult setMaster(ServiceEnvironment serviceEnvironment, ServiceEnvironment masterEnv) {
        HarmonyCommand command = new RedisFailoverCommand(serviceEnvironment, masterEnv);
        return executeCommand(command);
    }

    @Override
    public void moveMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkPreCondition(service, node, AdminAction.FAILOVER_MASTER);
                moveMasterImpl(service, node);
            }
        });
    }

    @Override
    public void forceMoveMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
                checkPreCondition(service, node, AdminAction.FAILOVER_MASTER);
                forceMoveMasterImpl(service, node);
            }
        });
    }

    @Override
    public void moveReader(final String service, final String node){
        // Do nothing
    }

//    @Override
//    public void forceMoveReader(final String service, final String node){
//        // Do nothing
//    }

    private void moveMasterImpl(String service, String targetNode) {
        Assert.notNull(service);
        Assert.notNull(targetNode);
        String currentMasterNode = clusterManager.getCurrentMaster(service);
        Assert.notNull(currentMasterNode);
        Assert.isTrue(!currentMasterNode.isEmpty());
        if (targetNode.equals(currentMasterNode)) {
            return;
        }

        ServiceEnvironment currentMasterSvcEnv = serviceConfigRepository.getServiceEnvironment(service, currentMasterNode);
        ServiceEnvironment newMasterSvcEnv = serviceConfigRepository.getServiceEnvironment(service, targetNode);

        try {
            //1. del VIP
            HarmonyCommandResult res = removeVIP(currentMasterSvcEnv, true);
            if (res.getResultType() == ResultType.FAILED) {
                throw new RuntimeException("Got exception from remove vip from " + currentMasterNode);
            }

            //2. kill connections
            res = killConnections(currentMasterSvcEnv);
            if (res.getResultType() == ResultType.FAILED) {
                throw new RuntimeException("Got exception from kill connections on " + currentMasterNode);
            }

            //3. wait at most 5 seconds primary slave to catch up
            long currentTimestamp = System.currentTimeMillis();
            RedisReplicationCommandResult replRes = (RedisReplicationCommandResult)getRedisReplicationStatus(newMasterSvcEnv);
            if (replRes.getResultType() == ResultType.FAILED) {
                throw new RuntimeException("Got exception from getting replication status from " + targetNode);
            }

            Assert.isTrue(replRes != null && replRes.getRedisReplicationStatusEntity() != null);
            while (System.currentTimeMillis() - currentTimestamp < 5000 &&
                    !replRes.isSyncCompleted()) {
                Thread.sleep(500);
                replRes = (RedisReplicationCommandResult)getRedisReplicationStatus(newMasterSvcEnv);
                if (replRes.getResultType() == ResultType.FAILED) {
                    throw new RuntimeException("Got exception from getting replication status from " + targetNode);
                }
            }

            if (!replRes.isSyncCompleted()) {
                throw new RuntimeException("Slave does not catch up in 5 seconds, aborting");
            }

            //4. set primary slave to master
            res = setMaster(newMasterSvcEnv, newMasterSvcEnv);
            if (res.getResultType() == ResultType.FAILED) {
                throw new RuntimeException("Got exception from setting master to " + targetNode + " on " + targetNode);
            }
            //5. set previous master to slave
            res = setMaster(currentMasterSvcEnv, newMasterSvcEnv);
            if (res.getResultType() == ResultType.FAILED) {
                throw new RuntimeException("Got exception from setting master to " + targetNode + " on " + currentMasterNode);
            }
            clusterManager.setCurrentPrimarySlave(service, currentMasterNode);

            //6. move vip to primary slave
            int retryCount = 5;
            while (retryCount >= 0) {
                res = placeVIP(newMasterSvcEnv, true);
                if (res.getResultType() == ResultType.SUCCEEDED) {
                    break;
                }
                retryCount--;
                Thread.sleep(500);
            }

            if (retryCount < 0) {
                throw new RuntimeException("Got exeption from place VIP on " + targetNode);
            }
            //*Any step is wrong, move vip back to original master
        } catch (Exception e) {
            logger.error("Got exception when doing failover:", e);
            logger.error("Doing rollback!", e);

            try {
                setMaster(newMasterSvcEnv, currentMasterSvcEnv);
                setMaster(currentMasterSvcEnv, currentMasterSvcEnv);
                removeVIP(newMasterSvcEnv, true);
                placeVIP(currentMasterSvcEnv, true);
                clusterManager.setCurrentPrimarySlave(service, targetNode);
            } catch (Exception ignore) {
                logger.error("Got exception when doing rolling back from failed failover", e);
                logger.error("Manual intervene required to fix the issue!");
            }

            throw new RuntimeException(e);
        }
    }

    private void forceMoveMasterImpl(String service, String targetNode) {
        Assert.notNull(service);
        Assert.notNull(targetNode);
        String currentMasterNode = clusterManager.getCurrentMaster(service);
        Assert.notNull(currentMasterNode); 
        Assert.isTrue(!currentMasterNode.isEmpty());
        if (targetNode.equals(currentMasterNode)) {
            return;
        }

        ServiceEnvironment currentMasterSvcEnv = serviceConfigRepository.getServiceEnvironment(service, currentMasterNode);
        ServiceEnvironment newMasterSvcEnv = serviceConfigRepository.getServiceEnvironment(service, targetNode);

        //1. del VIP
        try {
            removeVIP(currentMasterSvcEnv, true);
        } catch (Exception e) {
            logger.warn("Got exception from remove vip from " + currentMasterSvcEnv + ". IGNORED");
        }

        //2. kill connections
        try {
            killConnections(currentMasterSvcEnv);
        } catch (Exception e) {
            logger.warn("Got exception from kill connections on " + currentMasterSvcEnv + ". IGNORED");
        }

        //3. set primary slave to master
        try {
            setMaster(newMasterSvcEnv, newMasterSvcEnv);
        } catch (Exception e) {
            logger.error("Got exception from setting master to " + targetNode + " on " + ". IGNORED");
        }
        //4. set previous master to slave
        try {
            setMaster(currentMasterSvcEnv, newMasterSvcEnv);
        } catch (Exception e) {
            logger.warn("Got exception from setting master to " + targetNode + " on " + currentMasterNode + ". IGNORED");
        }
        //5. move vip to primary slave
        int retryCount = 5;
        while (retryCount >= 0) {
            HarmonyCommandResult res = placeVIP(newMasterSvcEnv, true);
            // Set previous master as primary slave.
            clusterManager.setCurrentPrimarySlave(newMasterSvcEnv.getService(),currentMasterSvcEnv.getNode());
            if (res.getResultType() == ResultType.SUCCEEDED) {
                break;
            }
            retryCount--;
            try {
                Thread.sleep(500);
            } catch (Exception ingore) {

            }
        }

        if (retryCount < 0) {
            throw new RuntimeException("Got exeption from place VIP on " + targetNode);
        }
    }



    @Override
    public void ensureMaster(final String service, final String node) {
        conductTransition(service, new Thrower<Throwable>() {
            @Override
            public void execute() throws Throwable {
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

        ServiceEnvironment targetSvcEnv = serviceConfigRepository.getServiceEnvironment(service, node);

        if (node.equals(currentMasterAlias)) {
            placeVIP(targetSvcEnv, false);
            logger.warn("Is Master: " + logDtl);
        } else {
            removeVIP(targetSvcEnv, false);
            killConnections(targetSvcEnv);
            logger.warn("Not Master: " + logDtl);
        }
    }

    @Override
    public void setServerStatus(String service, String node, ServiceNodeStatus status) {
        ZooKeeperService zkSvc = getZooKeeperService();
        String path = clusterManager.getStatusPath(service, node);
        zkSvc.setNodeStringData(path, status.name());
    }

    @Override
    public void activateServer(String service, String node) {}
    @Override
    public void inactivateServer(String service, String node) {}
    @Override
    public void onlineMaster(String service, String node) {}
    @Override
    public void offlineMaster(String service, String node) {}
    @Override
    public void onlineSlave(String service, String node, LinkLocation location) {
        Assert.notNull(service);
        Assert.notNull(node);
        String currentMasterNode = clusterManager.getCurrentMaster(service);
        Assert.notNull(currentMasterNode);
        Assert.isTrue(!currentMasterNode.isEmpty());
        if (node.equals(currentMasterNode)) {
            logger.error("Trying to set master to itself on " + node + ". IGNORED");
            return;
        }

        ServiceEnvironment masterSvcEnv = serviceConfigRepository.getServiceEnvironment(service, currentMasterNode);
        ServiceEnvironment slaveSvcEnv = serviceConfigRepository.getServiceEnvironment(service, node);
        try {
            setMaster(slaveSvcEnv, masterSvcEnv);
        } catch (Exception e) {
            logger.error("Got exception from setting master to " + masterSvcEnv.getNode() + " on " + slaveSvcEnv.getNode() + ". IGNORED");
        }
    }
    @Override
    public void offlineSlave(String service, String node) {}
    @Override
    public void assignRole(String service, String node, String role, LinkLocation location) {}
    @Override
    public void resignRole(String service, String node, String role) {}
}
