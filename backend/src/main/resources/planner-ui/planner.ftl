<html>
<style type="text/css">
    td {border:1px solid grey; font-size: x-small}
    th {border:1px solid grey; font-size: x-small}
    .oncall { background-color: LightPink;}
    .leaves { background-color: LightSkyBlue ;}
    .current { background-color: #DDDDDD;}
</style>
<body>
<table>
    <tr>
        <th>Employee/Week</th>
    <#list planner.plan.weeks as week>
        <th>${week.startDate.toString('dd-MMM')}:${week.endDate.toString('dd-MMM')}</th>
    </#list>
    </tr>

<#list planner.plan.getTeam().getTeamMember() as member>
    <tr>
        <td>${member.name}</td>
        <#list planner.getPlanForPerson(member) as pw>
            <td ${pw.isOncall()?then("class = 'oncall'",pw.hasLeaves()?then("class = 'leaves'",""))} ${pw.isCurrentWeek()?then("class = 'current'", "")}>
                <#if pw.isOncall() >
                    <b>On-call</b>

                <#else>
                    Leaves : <b>${pw.leaves}</b>
                    <#if pw.unoccupied() gt 0 >
                        <br> Avl b/w : <b>${pw.getAvailableBandWidth()}</b>
                    </#if>
                    <#if pw.okrList?has_content>
                        <br>
                        OKRs:
                        <#list pw.okrList as okr>
                            <li>${okr.description}</li>
                        </#list>
                    </#if>


                </#if>
            </td>
        </#list>
    </tr>

</#list>

</table>
<body>
</html>