/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.cluster;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

/**
 * User: leilin
 * Date: 10/2/14
 */
public class ZkTest {
    //@Test
    public void testZk() throws Exception{
        // test server
        TestingServer server = new TestingServer();

        // test client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), retryPolicy);
        client.start();

        client.create().forPath("/testroot", "testdata".getBytes());

        client.create().forPath("/testroot/testnode", "testnodedata".getBytes());

        String data = new String(client.getData().forPath("/testroot"));
        Object ret = client.getChildren().forPath("/testroot");


        System.in.read();

        client.close();

        server.close();
    }
}
