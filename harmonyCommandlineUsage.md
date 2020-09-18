## harmony_commandline usage:

1. ```cluster harmony_status <clusterName>```

get the cluster \<clusterName\> status.

2. ```cluster health_check <clusterName>```

check service status.

3. ```cluster get_status <clusterName>```

get cluster status.

4. ```cluster get_config <clusterName>```

get cluster config, including services, serviceNodes.

5. ```cluster set_online <clusterName> <serviceName> <nodeName>```

set online cluster.

6. ```cluster set_autofailover <clusterName> <service> [shadow|enabled|disabled]```

set autofailover status [shadow|enabled|disabled].

7. ```cluster get_autofailover_config <clusterName> <serviceName>```

get autofailover_config.

8. ```cluster set_autofailover_quota <clusterName> <serviceName> <newQuota>```

set autofailover quote number.

9. ```cluster set_mail_level <clusterName> [INFO|WARN|ERROR|NEVER]```

set cluster mail level status [INFO|WARN|ERROR|NEVER].

10. ```cluster get_mail_level <clusterName>```

get cluster mail level status.

11. ```clusters names```

get clusters names.

12. ```clusters health_check```

clusters health_check.

13. ```clusters set_mail_level [INFO|WARN|ERROR|NEVER]```

set clusters mail level status.

14. ```clusters harmony_health_check```

harmony health check.

15. ```clusters service_health_check```

service health check.

16. ```set echo [on|off]```

set echo switch.

17. ```get application```

get application name.

18. ```get universe```

get universe name.

19. ```get zkpr connection_string```

get zookeeper connection IP address.

#### Replication

1. ```cluster master_move_to <clusterName> <serviceName> <newMasterNodeName>```

move the master node to \<newMasterNodeName\>.

2. ```cluster force_master_move_to <clusterName> <serviceName> <newMasterNodeName>```

force move the master node to \<newMasterNodeName\>.

3. ```cluster fix_replication <clusterName> <serviceName>```

fix replication.

4. ```cluster reader_move_to <clusterName> <serviceName> <newReaderNodeName>```

move the reader to \<newReaderNodeName\>.