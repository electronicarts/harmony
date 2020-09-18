/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.zookeeper;

import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * User: leilin
 * Date: 10/8/14
 */
public interface ZooKeeperService {
    <TOut> TOut execute(Command<CuratorFramework, TOut> command);

    void execute(CommandNoReturn<CuratorFramework> command);

    void createNodeWithStringData(final String path, final String data);

    void ensurePath(final String path);

    String getNodeStringData(final String path);

    void setNodeStringData(final String path, final String data);

    Long getNodeLongData(final String path);

    void setNodeLongData(final String path, final Long data);

    List<String> getChildren(final String path);

    Stat checkExists(final String path);

    void delete(final String path);

    void deleteSubtree(final String path);

    InterProcessMutex createInterProcessMutex(String lockPath);

    LeaderLatch createLeaderLatch(String participantId, String latchPath, LeaderLatchListener listener);

    NodeCache createNodeCache(String nodePath, NodeCacheListener listener);

    void close();
}
