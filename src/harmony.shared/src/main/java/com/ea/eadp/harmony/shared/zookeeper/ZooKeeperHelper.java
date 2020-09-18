/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * User: leilin
 * Date: 10/7/14
 */
public class ZooKeeperHelper {
    public static String createNodeWithStringData(CuratorFramework client, String path, String data) {
        try {
            return client.create().forPath(path, data == null ? null : data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensurePath(CuratorFramework client, String path) {
        EnsurePath ensurer = new EnsurePath(path);
        try {
            ensurer.ensure(client.getZookeeperClient());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getNodeStringData(CuratorFramework client, String path) {
        byte[] data;
        try {
            data = client.getData().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Treat "" as null
        if (data.length == 0)
            return null;
        return new String(data);
    }

    public static void setNodeStringData(CuratorFramework client, String path, String data) {
        if (data == null) {
            data = "";
        }
        try {
            client.setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return;
    }

    public static Long getNodeLongData(CuratorFramework client, String path) {
        String str = getNodeStringData(client, path);
        return Long.parseLong(str);
    }

    public static void setNodeLongData(CuratorFramework client, String path, Long data) {
        String str = Long.toString(data);
        setNodeStringData(client, path, str);
    }

    public static List<String> getChildren(CuratorFramework client, String path) {
        List<String> data = null;
        try {
            data = client.getChildren().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static Stat checkExists(CuratorFramework client, String path) {
        Stat data;
        try {
            data = client.checkExists().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static void delete(CuratorFramework client, String path) {
        try {
            client.delete().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteSubtree(CuratorFramework client, String path) {
        Stat root = checkExists(client, path);
        if (root == null)
            return;
        List<String> children = getChildren(client, path);
        for (String child : children)
            deleteSubtree(client, path + "/" + child);
        delete(client, path);
    }
}