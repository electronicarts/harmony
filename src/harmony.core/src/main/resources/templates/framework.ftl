<#macro page>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>${mail_title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <style type="text/css">
        td, th, table {
            border: solid 1px black;
        }
    </style>
</head>
<body style="margin: 0; padding: 0;">
Basic information of this issue:
<table>
    <tr>
        <td>Issue description</td>
        <td>${root_cause}</td>
    </tr>
    <tr>
        <td>
            Universe
        </td>
        <td>
        ${universe}
        </td>
    </tr>
    <tr>
        <td>Affected cluster</td>
        <td>${cluster}</td>
    </tr>
    <tr>
        <td>Suggested Action</td>
        <td>${action}</td>
    </tr>
</table>
    <#nested/>
<p>Note: I know this email is ugly, still needs further beautify.</p>
</body>
</#macro>