/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.cluster;

import com.ea.eadp.harmony.cluster.entity.Cluster;
import com.ea.eadp.harmony.cluster.entity.ZookeeperORM.annotation.ZKPRMapping;
import com.ea.eadp.harmony.shared.format.Block;
import com.ea.eadp.harmony.shared.format.Board;
import com.ea.eadp.harmony.shared.format.Table;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;

import java.util.*;

public class MySQLCluster extends Cluster {
    @ZKPRMapping(path = "<<services>>", valueType = MySQLNodes.class)
    public Map<String, MySQLNodes> serviceNodes;

    @Override
    public String checkServiceStatus(TemplateRenderer render) {

        Map dataObjectModel = new HashMap();
        dataObjectModel.put("services", serviceNodes);
        dataObjectModel.put("cluster_roles", this.roles);
        return render.renderTemplate(getClass(), "mysql_command_output", dataObjectModel);
    }

    @Override
    public String checkServiceStatusInTable() {
        List<String> headersList = Arrays.asList("SERVICE", "NODE NAME", "STATUS", "ROLE", "DELAY", "IO RUNNING", "SQL RUNNING");
        List<List<String>> rowsList = new ArrayList<>();
        for (Map.Entry<String, MySQLNodes> serviceEntry : this.serviceNodes.entrySet()) {
            for (Map.Entry<String, MySQLNode> nodeEntry : serviceEntry.getValue().getNodes().entrySet()) {
                rowsList.add(Arrays.asList(
                        serviceEntry.getKey(),
                        nodeEntry.getKey(),
                        nodeEntry.getValue().markerStep.split("##").length > 0
                                ? ("OK".equals(nodeEntry.getValue().markerStep.split("##")[1]) ? "OK" : nodeEntry.getValue().markerStep.split("##")[2])
                                : "ERROR",
                        roles.get(serviceEntry.getKey()).getMaster().equals(nodeEntry.getKey()) ? "master" : "slave",
                        nodeEntry.getValue().properties.secondsBehindMaster,
                        nodeEntry.getValue().properties.slaveIoRunning,
                        nodeEntry.getValue().properties.slaveSqlRunning));
            }
        }


        Board board = new Board(75);

        Table table = new Table(board, 75, headersList, rowsList);
        List<Integer> colWidthsListEdited = Arrays.asList(10, 13, 8, 8, 9, 10, 11);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);

        Block block = table.tableToBlocks();
        board.setInitialBlock(block);
        return board.build().getPreview();
    }
}
