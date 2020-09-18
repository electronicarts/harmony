<#import "framework.ftl" as c/>
<@c.page>
<table>
    <tr>
        <td>Previously running count</td>
        <td>${old_count}</td>
    </tr>
    <tr>
        <td>Current running count</td>
        <td>${new_count}</td>
    </tr>
    <tr>
        <td>Current up nodes:</td>
        <td>
            <#list up_nodes as up_node>
                ${up_node},
            </#list>
        </td>
    </tr>
    <tr>
        <td>Down nodes</td>
        <#list down_nodes as down_node_name,down_node_hostname>
        <td>
                <p style="color:red"> [${down_node_name}:${down_node_hostname}] </p>
        </td>
        </#list>
    </tr>
</table>
</@c.page>