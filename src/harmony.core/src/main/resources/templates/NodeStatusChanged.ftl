<#import "framework.ftl" as c/>
<@c.page>
<table>
    <tr>
        <td>Hostname</td>
        <td>${hostname}</td>
    </tr>
    <tr>
        <td>Previous status</td>
        <td>${old_status}</td>
    </tr>
    <tr>
        <td>New status</td>
        <td>${new_status}</td>
    </tr>
</table>
</@c.page>