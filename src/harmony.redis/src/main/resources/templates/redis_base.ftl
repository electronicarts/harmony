<#macro redis>
    <#import "framework.ftl" as c/>
    <@c.page>

    <table>
        <tr>
            <td>Node:</td>
            <td>${config.getNode()}</td>
        </tr>
        <tr>
            <td>Host:</td>
            <td>${config.getHost()}</td>
        </tr>
        <tr>
            <td>Port:</td>
            <td>${config.getPort()}</td>
        </tr>
    </table>
        <#nested/>
    </@c.page>
</#macro>
