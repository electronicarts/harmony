<#import "console_func.ftl" as c/>
<#list harmonyNodesStatus.harmonyNodes as nodeName,nodeStatus>
Checking harmony status for cluster/node: ${cluster}/${nodeName}:
<#if harmonyNodeRunningStatus[nodeName]??>
    ProcessRunning: <@c.OK>Yes</@c.OK> <#if (nodeName==harmonyNodesStatus.harmonyLeader)> Leader</#if>
    LastClusterInspectionTime: <@c.checkERROR (nodeStatus.lastClusterCheckTime?number - .now?long < CHECK_TIME_OUT) nodeStatus.lastClusterCheckTime?number?number_to_date?string("YYYY-MM-dd HH:mm:ss")/>
    LastNodesInspectionTime: <@c.checkERROR (nodeStatus.lastNodeInspectionTime?number - .now?long < CHECK_TIME_OUT) nodeStatus.lastNodeInspectionTime?number?number_to_date?string("YYYY-MM-dd HH:mm:ss")/>
<#else>
    ProcessRunning: <@c.ERROR>No</@c.ERROR>
</#if>
</#list>

<#list nodeHoldingWriterVipForService as serviceName, vipNode>
Checking writer VIP status for cluster/service:${cluster}/${serviceName}
<#if clusterStatus.roles[serviceName]??>
    Current master: <@c.checkERROR (clusterStatus.roles[serviceName].master??) clusterStatus.roles[serviceName].master!"NOTSET"/>
    <#if !vipNode??>
    Current writer VIP holder: <@c.ERROR>NOTSET</@c.ERROR>
    <#elseif vipNode?contains(",")>
    Current writer VIP holder: <@c.ERROR>Vip conflict, vip set on more than one node: ${vipNode}</@c.ERROR>
    <#elseif clusterStatus.roles[serviceName].master?? && (clusterStatus.roles[serviceName].master != vipNode)>
    Current writer VIP holder: <@c.ERROR>VIP is assigned to: ${vipNode}, but master is set on another node.</@c.ERROR>
    <#else>
    Current writer VIP holder: <@c.OK>${vipNode}</@c.OK>
    </#if>
<#else>
    <@c.ERROR> Service information wrong.</@c.ERROR>
</#if>
</#list>

<#list nodeHoldingReaderVipForService as serviceName, vipNode>
Checking reader VIP status for cluster/service:${cluster}/${serviceName}
    <#if !vipNode??>
    Current reader VIP holder: <@c.ERROR>NOTSET</@c.ERROR>
    <#elseif vipNode?contains(",")>
    Current writer VIP holder: <@c.ERROR>Vip conflict, vip set on more than one node: ${vipNode}</@c.ERROR>
    <#else>
    Current reader VIP holder: <@c.OK>${vipNode}</@c.OK>
    </#if>
</#list>

