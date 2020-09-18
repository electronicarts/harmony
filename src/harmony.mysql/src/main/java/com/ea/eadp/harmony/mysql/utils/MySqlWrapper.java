/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.utils;

import com.ea.eadp.harmony.mysql.config.MySqlServiceConfig;
import com.ea.eadp.harmony.mysql.entity.MasterStatusDB;
import com.ea.eadp.harmony.mysql.entity.SlaveStatusDB;
import com.ea.eadp.harmony.shared.utils.Command;
import com.ea.eadp.harmony.shared.utils.CommandNoReturn;
import com.ea.eadp.harmony.shared.utils.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class MySqlWrapper {

    private String host;
    private int port;

    private String connectionUrl;

    private String username;
    private String password;

    public MySqlWrapper(String host,
                        int port,
                        String username,
                        String password,
                        Map<String, String> jdbcUrlParams) {
        this.host = host;
        this.port = port;
        this.connectionUrl = getConnectionUrl(host, port, jdbcUrlParams);
        this.username = username;
        this.password = password;
    }

    public MySqlWrapper(MySqlServiceConfig config) {
        this(config.getHost(),
                config.getPort(),
                config.getMonitorUser(),
                config.getMonitorPassword(),
                config.getJdbcUrlParams());
    }

    private final static Logger logger = LoggerFactory.getLogger(MySqlWrapper.class);

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    static {
        loadDriver();
    }

    private static String getConnectionUrl(String host, int port, Map<String, String> jdbcUrlParams) {
        String connectionUrl = "jdbc:mysql://" + host + ":" + port;
        boolean isFirstParam = true;
        for (Map.Entry<String, String> paramEntry : jdbcUrlParams.entrySet()) {
            connectionUrl += isFirstParam ? "?" : "&";
            isFirstParam = false;
            connectionUrl += paramEntry.getKey() + "=" + paramEntry.getValue();
        }
        return connectionUrl;
    }

    public static Connection getConnection(String url, String username, String password) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
        return conn;
    }

    public static <TOut> TOut executeQuery(Connection conn, String sql,
                                           Transformer<ResultSet, TOut, SQLException> transformer) {
        Statement stmt = null;
        ResultSet rs;
        TOut ret = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (!rs.first())
                throw new RuntimeException("Cannot get a row from database.");
            ret = transformer.execute(rs);
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
                stmt = null;
            }
        }
        return ret;
    }

    public static void executeBatch(Connection conn, String[] sqls) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for (String sql : sqls) {
                stmt.addBatch(sql);
            }
            stmt.executeBatch();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
                stmt = null;
            }
        }
    }

    public static String getSystemVariable(Connection conn, String name) {
        return executeQuery(conn, String.format("show variables like '%s'", name),
                new Transformer<ResultSet, String, SQLException>() {
                    public String execute(ResultSet input) throws SQLException {
                        return input.getString("Value");
                    }
                });
    }

    public static SlaveStatusDB getSlaveStatus(Connection conn) {
        return executeQuery(conn, "show slave status",
                new Transformer<ResultSet, SlaveStatusDB, SQLException>() {
                    public SlaveStatusDB execute(ResultSet input) throws SQLException {
                        return new SlaveStatusDB(
                                input.getString("Master_Log_File"),
                                input.getLong("Exec_Master_Log_Pos"),
                                input.getLong("Seconds_Behind_Master"),
                                input.getString("Slave_IO_Running"),
                                input.getString("Slave_SQL_Running"));
                    }
                });
    }

    public static MasterStatusDB getMasterStatus(Connection conn) {
        return executeQuery(conn, "show master status",
                new Transformer<ResultSet, MasterStatusDB, SQLException>() {
                    public MasterStatusDB execute(ResultSet input) throws SQLException {
                        return new MasterStatusDB(
                                input.getString("File"),
                                input.getLong("Position"));
                    }
                });
    }

    public static void runWarmUp(Connection conn, String[] sqls) {
        executeBatch(conn, sqls);
    }

    public static void killWarmUp(Connection conn, String username) {
        int rc = killConnections(conn, username, "identity");
        logger.info("Killed {} warm up connections", rc);
    }

    public static int killConnections(Connection conn, String ofUser) {
        return killConnections(conn, ofUser, null);
    }

    public static int killConnections(Connection conn, String ofUser, String ofDb) {
        String[] usrArr = ofUser.split(",");
        Set<String> usrSet = new HashSet<String>(Arrays.asList(usrArr));

        Statement stmt = null;
        ResultSet rs;
        int rc = 0;
        String sql = "show processlist";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String usr = rs.getString("User");
                if (ofDb != null) {
                    String db = rs.getString("db");
                    if (db == null || !db.equalsIgnoreCase(ofDb)) {
                        continue;
                    }
                }
                if (usrSet.contains(usr)) {
                    rc += 1;
                    Long cid = rs.getLong("Id");
                    sql = "kill connection " + cid;
                    executeStatement(conn, sql);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
                stmt = null;
            }
        }
        return rc;
    }

    public static boolean executeStatement(Connection conn, String sql) {
        Statement stmt = null;
        boolean ret = false;
        try {
            stmt = conn.createStatement();
            ret = stmt.execute(sql);
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
                stmt = null;
            }
        }
        return ret;
    }

    public Connection getConnection() {
        return getConnection(this.connectionUrl, this.username, this.password);
    }

    public <TOut> TOut execute(Command<Connection, TOut> command) {
        Connection conn = null;
        try {
            conn = getConnection();
            return command.execute(conn);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.error("Error close connection", e);
                }
            }
        }
    }

    public void execute(CommandNoReturn<Connection> command) {
        Connection conn = null;
        try {
            conn = getConnection(this.connectionUrl, this.username, this.password);
            command.execute(conn);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.error("Error close connection", e);
                }
            }
        }
    }
}