/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.zookeeper;

import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * User: leilin
 * Date: 10/7/14
 */
@Component
public class ZooKeeperServiceImpl implements InitializingBean, ZooKeeperService {
    private final static Logger logger = LoggerFactory.getLogger(ZooKeeperServiceImpl.class);

    @Autowired
    private ZooKeeperConfig config;

    private RetryPolicy retryPolicy;

    private CuratorFramework client;

    public ZooKeeperConfig getConfig() {
        return config;
    }

    public void setConfig(ZooKeeperConfig config) {
        this.config = config;
    }

    private CuratorFramework createClient() {
        try {
            logger.info("Creating zookeeper client to:" + config.getConnectionString());
            CuratorFramework client = CuratorFrameworkFactory.newClient(config.getConnectionString(), retryPolicy);
            client.start();
            client.getZookeeperClient().blockUntilConnectedOrTimedOut();
            logger.info("Zookeeper client created!");
            return client;
        } catch (Exception ex) {
            logger.error("Something wrong happened, can't create zookeeper client", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <TOut> TOut execute(Command<CuratorFramework, TOut> command) {
        return command.execute(client);
    }

    @Override
    public void execute(CommandNoReturn<CuratorFramework> command) {
        command.execute(client);
    }

    @Override
    public void createNodeWithStringData(final String path, final String data) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.createNodeWithStringData(client, path, data);
            }
        });
    }

    @Override
    public void ensurePath(final String path) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.ensurePath(client, path);
            }
        });
    }

    @Override
    public String getNodeStringData(final String path) {
        return execute(new Command<CuratorFramework, String>() {
            @Override
            public String execute(CuratorFramework client) {
                return ZooKeeperHelper.getNodeStringData(client, path);
            }
        });
    }

    @Override
    public void setNodeStringData(final String path, final String data) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.setNodeStringData(client, path, data);
            }
        });
    }

    @Override
    public Long getNodeLongData(final String path) {
        return execute(new Command<CuratorFramework, Long>() {
            @Override
            public Long execute(CuratorFramework client) {
                return ZooKeeperHelper.getNodeLongData(client, path);
            }
        });
    }

    @Override
    public void setNodeLongData(final String path, final Long data) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.setNodeLongData(client, path, data);
            }
        });
    }

    @Override
    public List<String> getChildren(final String path) {
        return execute(new Command<CuratorFramework, List<String>>() {
            @Override
            public List<String> execute(CuratorFramework client) {
                return ZooKeeperHelper.getChildren(client, path);
            }
        });
    }

    @Override
    public Stat checkExists(final String path) {
        return execute(new Command<CuratorFramework, Stat>() {
            @Override
            public Stat execute(CuratorFramework client) {
                return ZooKeeperHelper.checkExists(client, path);
            }
        });
    }

    @Override
    public void delete(final String path) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.delete(client, path);
            }
        });
    }

    @Override
    public void deleteSubtree(final String path) {
        execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.deleteSubtree(client, path);
            }
        });
    }

    @Override
    public InterProcessMutex createInterProcessMutex(String lockPath) {
        return new InterProcessMutex(client, lockPath);
    }

    @Override
    public LeaderLatch createLeaderLatch(String participantId, String latchPath, LeaderLatchListener listener) {
        LeaderLatch leaderLatch = new LeaderLatch(client, latchPath, participantId);
        leaderLatch.addListener(listener);
        try {
            leaderLatch.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return leaderLatch;
    }

    @Override
    public NodeCache createNodeCache(String nodePath, NodeCacheListener listener) {
        NodeCache nodeCache = new NodeCache(client, nodePath);
        nodeCache.getListenable().addListener(listener);
        try {
            nodeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return nodeCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        retryPolicy = new ExponentialBackoffRetry(config.getRetrySleep(), config.getMaxRetry());
        client = createClient();
    }

    @Override
    public void close() {
        logger.info("Shutting down zkpr client.");
        if (client != null && client.getState() == CuratorFrameworkState.STARTED) {
            client.close();
            logger.info("zkpr client shutdown.");
        } else {
            logger.info("zkpr client is null or not running!");
        }
    }
}
