<#macro fg color>${'\x1B'}[${30 + color}m</#macro>
<#macro RESET>${'\x1B'}[0m</#macro>
<#macro BLACK>${'\x1B'}<@fg 0/></#macro>
<#macro RED>${'\x1B'}<@fg 1/></#macro>
<#macro GREEN>${'\x1B'}<@fg 2/></#macro>
<#macro YELLOW>${'\x1B'}<@fg 3/></#macro>
<#macro BLUE>${'\x1B'}<@fg 4/></#macro>
<#macro MAGENTA>${'\x1B'}<@fg 5/></#macro>
<#macro CYAN>${'\x1B'}<@fg 6/></#macro>
<#macro WHITE>${'\x1B'}<@fg 7/></#macro>

<#macro WARNING><@YELLOW/><#nested><@RESET/></#macro>
<#macro ERROR><@RED/><#nested><@RESET/></#macro>
<#macro OK><@GREEN/><#nested><@RESET/></#macro>

<#macro checkERROR condition value><#if condition> <@OK>${value}</@OK><#else><@ERROR>${value}</@ERROR></#if></#macro>
<#macro checkWARNING condition value><#if condition> <@OK>${value}</@OK><#else><@WARNING>${value}</@WARNING></#if></#macro>
