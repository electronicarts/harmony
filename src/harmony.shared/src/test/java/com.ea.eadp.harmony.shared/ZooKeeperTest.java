/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared;

import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import com.ea.eadp.harmony.shared.zookeeper.*;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * User: leilin
 * Date: 10/7/14
 */
public class ZooKeeperTest {
    private static TestingServer server;
    private static ZooKeeperServiceImpl service;

    @BeforeClass
    public static void init() throws Exception {
        // test server
        server = new TestingServer();

        ZooKeeperConfig config = new ZooKeeperConfig();
        config.setConnectionString(server.getConnectString());
        config.setMaxRetry(3);
        config.setRetrySleep(1000);

        service = new ZooKeeperServiceImpl();
        service.setConfig(config);
        service.afterPropertiesSet();
    }

    @AfterClass
    public static void clean() throws Exception {
        server.close();
    }

    @Test
    public void testZk() throws Exception {
        final String value = "testvalue";

        service.execute(new CommandNoReturn<CuratorFramework>() {
            @Override
            public void execute(CuratorFramework client) {
                ZooKeeperHelper.createNodeWithStringData(client, "/test", value);
            }
        });

        String val = service.execute(new Command<CuratorFramework, String>() {
            @Override
            public String execute(CuratorFramework client) {
                return ZooKeeperHelper.getNodeStringData(client, "/test");
            }
        });

        Assert.assertEquals(value, val);
    }
}
