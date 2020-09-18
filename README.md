# EA harmony

EA harmony is a monitor program for the distributed database system, such as mysql and redis. Harmony supports autofailover and replication from the master node to slave node. When the master node is down, harmony can migrate data to the slave node and set the slave node as master node by using harmony commandlines.

## Prerequisites

You need to set up zookeeper and mysql/redis instances on your system before harmony setup. 

ZooKeeper : See [ZooKeeper Installation](http://www.corejavaguru.com/bigdata/zookeeper/getting-started-with-zookeeper).

MySQL : See [MySQL Installation](https://support.rackspace.com/how-to/install-mysql-server-on-the-ubuntu-operating-system/).

Redis : See [Redis Installation](https://redis.io/download).

Harmony need to set up at least two mysql instances. The first one will be set up as master node and the second one as slave node.

Following README provides an example, which sets up two mysql instances in one centos system and their corresponding harmony instances, for simplicity. If you want to use harmony in real world production environment, you should set up harmony instance on each database server.

#### Following is a harmony setup example for mysql.

In this example, we choose `mysql` as harmony.application, `testEnvironment` as harmony.universe, `testCluster` as hamrony.clusterType and `testCluster_001` as cluster name. In this case, you will set up two harmony instances for each mysql as master node and slave node. The master node's name is set as `db01` and slave node's name is `db02`.

#### Create mysql user & grant privileges

``` sql
CREATE USER '{your mysql username}'@'%' IDENTIFIED BY '{your mysql password}';
GRANT RELOAD, PROCESS, SUPER, REPLICATION CLIENT ON *.* TO '{your mysql username}'@'%';
```

You can use the default username : violin and password : Password

#### Set your own mysql username & password

If you want to use your own username and password, you need to change the default.properties.

``` sh
vim harmony.configuration.data/src/main/resources/mysql/default.properties
```

Replace service.mysql.user and service.mysql.password with following configurations:

``` sh
service.mysql.user = {your mysql username}
service.mysql.password = {your mysql password}
```

#### default.properties structure

Here is the structure of the directory: harmony.configuration.data/src/main/resources/

```
../resources
│   default.properties    
│
└───mysql
    │   default.properties
    │
    └───testEnvironment
        │   default.properties
        │   
        └───testCluster   
            │   default.properties
            │   ...
```

There are four layers of default.properties in configuration.data. The default.properties are configurations of Harmony, including Zookeeper, Monitoring, Command, Email Service, Spring, MySQL/Redis and Harmony Instances settings. Besides, The inner layer properties will override outer layer properties. 

Here's the four layer properties usage:

- resources/default.properties: You can set Zookeeper, Monitoring, Command and Email Service configuration in this properties. 
- mysql/default.properties: In this layer you can set Spring, Harmony process, MySQL Service in configuration. You can set MySQL user password in this properties.
- testEnvironment/default.properties: In this layer you can set Auto failover configuration including autofailover.mode, grace maxQuota and fresh time.
- testCluster/default.properties: Per cluster properties. In this layer you can set harmony instance configuration in each cluster.

## Installation

### 1. Get the harmony source code.

git clone from this repository or download the source code into your Linux OS.

### 2. Set default.properties

Change the harmony.configuration.data/src/main/resources/mysql/testEnvironment/testCluster/default.properties

``` sh
cd harmony-v2/src/
vim harmony.configuration.data/src/main/resources/mysql/testEnvironment/testCluster/default.properties
```

In this example, we set up a cluster named testCluster_001, with master node named db01 and slave node named db02.

Add following configurations into default.properties:

``` sh
#harmony instance 1: 
testCluster_001.db01._p_.harmony.server.port = 8087 
#harmony instance 2: 
testCluster_001.db02._p_.harmony.server.port = 8088 
```

The harmony.server.port of each harmony instance should be same as HARMONY_PORT in step 6.

### 3. Build harmony.app-*.tar.gz

``` sh
mvn clean install
cd harmony.app/target
tar -zxvf harmony.app-*.tar.gz
```

<!--the unzipped directory structure is shown below:

```
project
└───bin
└───conf
└───scratch
└───serv
```-->

Copy this directory to where you want to build harmony, or just build in this directory. In this example, you need to build two harmony instances for master node and slave node. Each harmony instance need a independent build directory to set up.

``` sh
cp -r ./* {harmony build dir}/harmony/
cd {harmony build dir}/harmony/
```

### 4. Generate Certificates & Set config.key

Considering of the database security, harmony need certificates to authenticate the identity of database user.

#### Generate the certificates

``` sh
cp scratch/cert_generator.sh ./
chmod +x cert_generator.sh
```

<!-- Replace ```wildcard_name=*.${domain}``` with ```wildcard_name=*```-->

Run `cert_generator.sh` to generate cerificates.

``` sh
./cert_generator.sh conf/ {your hostname}
```

You can get your hostname by shell command `hostname`.

#### Set the config.key

``` sh
vim conf/config.key
```

Add `abcdefghijklmnopqrstuvwx` or other 24-bit random string into config.key

### 5. Set harmony-override.properties

You need to set `conf/harmony-override.properties` to override properties in harmony configuration.

``` sh
vim conf/harmony-override.properties
```

Here is the master node conf/harmony-override.properties:

``` sh
harmony.env.application=mysql
harmony.env.universe=testEnvironment
harmony.env.clusterType=testCluster
harmony.zookeeper.connectionString={your zookeeper server ip}:2181
harmony.env.cluster=testCluster_001
harmony.env.node=db01
service.host={your first mysql hostname}
service.port={your first mysql port}
``` 

In this example harmony-override.properties:
 
`mysql` is set as harmony.application.
`testEnvironment` is set as harmony.universe.
`testCluster` is set as harmony.clusterType.
`testCluster_001` is set as the clusterNames.

`db01` is set as the node name of master node, and `db02` is set as the slave node's name in slave node's harmony-override.properties below.

You need to set `{your zookeeper server ip}:2181` as connectionString. Besides, you need to set your mysql host ip and port as service.host and port here.

<!--

harmony.env.application=mysql
harmony.env.universe=testEnvironment
harmony.env.clusterType=testCluster
harmony.zookeeper.connectionString={your zookeeper host ip}:2181
harmony.env.cluster=testCluster_001
harmony.env.node=db01
service.host=localhost.localdomain
service.port=3306

-->

### 6. Start harmony instance

``` sh
cp scratch/harmony ./
chmod +x harmony
vim harmony
```

Add following configurations into harmony script:

``` sh
HARMONY_BASE="./"
HARMONY_USER="root"
HARMONY_PORT=8087
```

HARMONY_BASE means where to set up harmony instance. HARMONY_USER means which system user to set up harmony(defaultly using "root"). HARMONY_PORT means which port to set up harmony instance. In this example, we build two harmony instance in one system, so we need to choose two different port to set up harmony. If you set up harmony in different hosts, you can just set up them on the same port, besides you need to keep it same as default.properties configurations in step 2.

Run the harmony script to start master node harmony instance:

``` sh
./harmony start
```

Then you have set up the master node harmony instance already, you can check it by ```ps -ef | grep harmony```.

#### Set up another harmony instance for slave node by following steps:

Each harmony instance need a independent directory to set up, so you need mkdir a new directory to set up the slave node harmony instance.

``` sh
cd ..
mkdir harmony_db02
cp -r harmony/* harmony_db02
cd harmony_db02/
```

Change the conf/harmony-override.properties:
Here is the slave node conf/harmony-override.properties:

``` sh
harmony.env.application=mysql
harmony.env.universe=testEnvironment
harmony.env.clusterType=testCluster
harmony.zookeeper.connectionString={your zookeeper host ip}:2181
harmony.env.cluster=testCluster_001
harmony.env.node=db02
service.host={your second mysql hostname}
service.port={your second mysql port}
```

Change the harmony script:

``` sh
vim harmony
```

Add following configurations into harmony script:

Slave Node harmony script:

``` sh
HARMONY_BASE="./"
HARMONY_USER="root"
HARMONY_PORT=8088
```

Run the harmony script to start slave node harmony instance:

``` sh
./harmony start
```

PS: Please check the serv/harmony_app.log when you meet some running problem.

## Usage

Run harmony_commandline.sh

``` sh
cd scratch/
./harmony_commandline
```

In this example above, you can use `mysql` as \<serviceName>, `testCluster_001` as \<clusterName> and `db01|db02` as \<nodeName> in harmony commandline. You can check it according to default.properties and harmony-override.properties.

You can use `Tab` to see which parameters should be used by harmony commandline.

You can use the commandline below as eaxmples:

``` 
harmony\testEnvironment\testCluster\mysql>clusters health_check
harmony\testEnvironment\testCluster\mysql>cluster set_online testCluster_001 mysql db01
harmony\testEnvironment\testCluster\mysql>cluster master_move_to testCluster_001 mysql db02
```

#### harmony commandline usage:

See the harmonyCommandlineUsage.md

## Contributing

See the CONTRIBUTING.md

## License

Modified BSD License (3-Clause BSD license) see the file LICENSE in the project root.
