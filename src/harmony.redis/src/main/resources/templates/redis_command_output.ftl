<#import "console_func.ftl" as c/>
Checking Redis Service Status.
<#list services as serviceName,serviceValue>
    Checking service: ${serviceName}
    Service master node name: ${cluster_roles[serviceName].master!"NOTSET"}
    Service slave node name: ${cluster_roles[serviceName].primary_slave!"NOTSET"}
    <#list serviceValue.nodes as nodeName, nodeValue>
        Checking node: ${nodeName}
        <#if nodeValue.marker??>
        <#--Service status information-->
            <#switch nodeValue.marker>
                <#case "GENERIC_INF_SVR">
                ServiceStatus: <@c.OK>OK</@c.OK>
                    <#break>
                <#default>
                ServiceStatus: <@c.WARNING>${nodeValue.marker!"NOTSET"}</@c.WARNING>
                StatusDetail: <@c.WARNING>${nodeValue.detail!"NOTSET"}</@c.WARNING>
                Suggested Action: <@c.WARNING>${nodeValue.action!"NOTSET"}</@c.WARNING>
            </#switch>
        <#-- Redis properties -->
                Replication role: ${nodeValue.properties.role}
                <#if nodeValue.properties.role == "master">
                Connected slaves:<@c.checkERROR (nodeValue.properties.connected_slaves?number > 0) nodeValue.properties.connected_slaves/>
                <#elseif nodeValue.properties.role == "slave">
                Replication master host: ${nodeValue.properties.master_host}:${nodeValue.properties.master_port}
                Master link status: <@c.checkERROR (nodeValue.properties.master_link_status == "up") nodeValue.properties.master_link_status></@c.checkERROR>
                <#else>
                Error: <@c.ERROR>Replication role unknown: ${nodeValue.properties.role}</@c.ERROR>
                </#if>
        <#else>
                <@c.ERROR>Node marker not set, status unknown</@c.ERROR>
        </#if>
    <#else>
        <@c.ERROR>No node defined in this service: ${serviceName}</@c.ERROR>
    </#list>
<#else>
    <@c.ERROR>No service in this node.</@c.ERROR>
</#list>