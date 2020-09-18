<#import "mysql_base.ftl" as c/>
<@c.mysql>
<#if error??>
    Error happened, Failover failed: ${error}
</#if>
</@c.mysql>

