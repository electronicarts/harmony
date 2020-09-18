<#import "console_func.ftl" as c/>
Checking MySQL Service Status.
<#list services as serviceName,serviceValue>
    Checking service: ${serviceName}
    Service master node name: ${cluster_roles[serviceName].master!"NOTSET"}
    Service slave node name: ${cluster_roles[serviceName].primary_slave!"NOTSET"}
    <#list serviceValue.nodes as nodeName, nodeValue>
            Checking node: ${nodeName}
        <#attempt>
            ServiceStatus: <@c.checkERROR ("OK"==nodeValue.markerStep?split("##")[1]) nodeValue.markerStep?split("##")[1]/>
            HarmonyStatus: <@c.checkERROR ("ONLINE"==nodeValue.status) nodeValue.status/>
            <#if ("OK" !=nodeValue.markerStep?split("##")[1])>
            Suggested Action: <@c.WARNING>${nodeValue.markerStep?split("##")[2]!"NOTSET"}</@c.WARNING>
            <#else>
            SecondsBehindMaster: <@c.checkERROR (nodeValue.properties.secondsBehindMaster?number >= 0) nodeValue.properties.secondsBehindMaster/>
            SlaveIoThreadRunning: <@c.checkERROR (nodeValue.properties.slaveIoRunning == 'Yes') nodeValue.properties.slaveIoRunning/>
            SlaveSqlThreadRunning: <@c.checkERROR (nodeValue.properties.slaveSqlRunning == 'Yes') nodeValue.properties.slaveSqlRunning/>
            </#if>
        <#recover>
            <@c.ERROR>Status of this node is abnormal</@c.ERROR>
        </#attempt>

    </#list>
</#list>