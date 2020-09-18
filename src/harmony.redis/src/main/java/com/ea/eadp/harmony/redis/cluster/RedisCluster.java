/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.redis.cluster;

import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;
import com.ea.eadp.harmony.shared.format.Block;
import com.ea.eadp.harmony.shared.format.Board;
import com.ea.eadp.harmony.shared.format.Table;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by VincentZhang on 5/8/2018.
 */
public class RedisCluster extends Cluster {
    @ZKPRMapping(path = "<<services>>", valueType = RedisNodes.class)
    public Map<String, RedisNodes> serviceNodes;

    @Override
    public String checkServiceStatus(TemplateRenderer render) {
        Map dataObjectModel = new HashMap();
        dataObjectModel.put("services", serviceNodes);
        dataObjectModel.put("cluster_roles", this.roles);
        return render.renderTemplate(getClass(), "redis_command_output", dataObjectModel);
    }

    @Override
    public String checkServiceStatusInTable() {
        List<String> headersList = Arrays.asList("SERVICE", "NODE NAME", "STATUS", "ROLE", "DESCRIPTION");
        List<List<String>> rowsList = new ArrayList<>();
        for (Map.Entry<String, RedisNodes> serviceEntry : this.serviceNodes.entrySet()) {
            for (Map.Entry<String, RedisNode> nodeEntry : serviceEntry.getValue().getNodes().entrySet()) {
                String description = "";
                if (!"GENERIC_INF_SVR".equals(nodeEntry.getValue().marker)) {
                    if (StringUtils.isNotBlank(nodeEntry.getValue().marker)) {
                        description = nodeEntry.getValue().getDetail();
                    } else {
                        description = "Service status unknown";
                    }
                } else {
                    switch(nodeEntry.getValue().properties.role) {
                        case "master":
                            description = String.format("Connected slaves: %s", nodeEntry.getValue().properties.connected_slaves);
                            break;
                        case "slave":
                            description = String.format("Status: %s", nodeEntry.getValue().properties.master_link_status);
                            break;
                        default:
                            description = "Replication status unknown";
                    }
                }
                rowsList.add(Arrays.asList(
                        serviceEntry.getKey(),
                        nodeEntry.getKey(),
                        "GENERIC_INF_SVR".equals(nodeEntry.getValue().marker) ? "OK" : "ERROR",
                        nodeEntry.getValue().properties.role,
                        description));
            }
        }


        Board board = new Board(75);

        Table table = new Table(board, 75, headersList, rowsList);
        List<Integer> colWidthsListEdited = Arrays.asList(10, 13, 8, 8, 30);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);

        Block block = table.tableToBlocks();
        board.setInitialBlock(block);
        return board.build().getPreview();
    }
}
