<html>
    <style type="text/css">
        div {border:1px solid grey; font-size: x-small}
        .oncall { background-color: LightPink;}
        .leaves { background-color: LightSkyBlue ;}
        .column { float: left;
            width:"#{(90 /planner.plan.getTeam().getTeamMember()?size)?floor}%"
            float: left;
            position: relative;
            width: -webkit-calc(#{(90/planner.plan.getTeam().getTeamMember()?size)?floor}%);
        }
        .cell {
            height: -webkit-calc(#{(90 /planner.plan.weeks?size)?floor}%);
            height: "#{(90 /planner.plan.weeks?size)?floor}%"

        }
        .table {
            width = "100%"
            width = -webkit-calc(100%);
            height = "100%"
            heigth = -webkit-calc(100%);
        }

    </style>
    <body>
    <div class="table">
        <div class ="column">
             <div class ="cell"> Week/Employee</div>
                    <#list planner.plan.weeks as week>
                    <div class ="cell">${week.startDate.toString('dd-MMM')}:${week.endDate.toString('dd-MMM')}</div>
                    </#list>
        </div>
            <#list planner.plan.getTeam().getTeamMember() as member>
            <div  class ="column">
                <div class ="cell">${member.name}</div>
                <#list planner.getPlanForPerson(member) as pw>
                    <div class = "${pw.isOncall()?then("oncall",pw.hasLeaves()?then("leaves",""))}" class ="cell">
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
                    </div>
                </#list>
            </div>
            </#list>
    </div>
    <body>
</html>