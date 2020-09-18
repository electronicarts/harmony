<#import "framework.ftl" as c/>
<@c.page>
<table>
    <tr>
        <td>Zkpr node address</td>
        <td>${zkpr_connection_string}</td>
    </tr>
    <tr>
        <td>Zkpr path</td>
        <td>${zkpr_path}</td>
    </tr>
    <tr>
        <td>Current harmony node address</td>
        <td>${harmony_node_address}</td>
    </tr>
    <tr>
        <td>Harmony time</td>
        <td>${harmony_time}</td>
    </tr>
    <tr>
        <td>ZKPR time</td>
        <td>${zkpr_time}</td>
    </tr>
    <tr>
        <td>Time difference</td>
        <td><p style="color:red"> ${time_difference} ms </p></td>
    </tr>
</table>
</@c.page>

