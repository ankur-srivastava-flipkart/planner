<html>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular.min.js"></script>
<script src="/planner-ui/datagrid/pagination.min.js"></script>
<script src="/planner-ui/datagrid/dataGrid.min.js"></script>
<style type="text/css">
    td {border:1px solid grey; font-size: x-small}
    th {border:1px solid grey; font-size: x-small}
    p {font-size: x-small}
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
                        <#if pw.leaves gt 0 >
                            Leaves : <b>${pw.leaves}</b>
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

<div ng-app = "bandwidthCaclulator" ng-controller = "bandwidthFinder">
    <p>Remaining Available Bandwidth (Not counting week of <input type="date" ng-model="asOfDate" ng-change="textChanged()" name="asOfDate" />) - {{availableBandwidth}} person days ({{availableBandwidth/5}} weeks).</p>
</div>
<div id="module2" ng-app = "okrTable" ng-controller = "okrController">
    <div grid-data grid-options="gridOptions" grid-actions="gridActions" server-pagination="true">
        <table class="table">
            <thead>
            <tr>
                <th class="sortable">
                    Description
                </th>
                <th class="sortable">
                    Jira Epic
                </th>
                <th class="sortable">
                    effortinPersonDays
                </th>
                <th class="sortable">
                    Complexity
                </th>
                <th class="sortable">
                    Priority
                </th>
                <th class="sortable">
                    parallelism
                </th>
                <th class="sortable">
                    willSpill
                </th>

            </tr>
            </thead>
            <tbody>
            <tr grid-item>
                <td ng-bind="item.description"></td>
                <td ng-bind="item.jiraEpic"></td>
                <td ng-bind="item.effortinPersonDays"></td>
                <td ng-bind="item.complexity"></td>
                <td ng-bind="item.priority"></td>
                <td ng-bind="item.parallelism"></td>
                <td ng-bind="item.willSpill"></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<script>

    angular.module('bandwidthCaclulator', [])
            .controller('bandwidthFinder', function ($scope, $http, $filter) {

                $scope.textChanged = function() {
                    var url = "/planner/${planner.plan.getTeam().getName()}/${planner.plan.getQuarter()}/action";
                    $http.post(url,{ "action": "GET_REMAINING_BANDWIDTH", "param" : { "DATE" : $scope.asOfDate == null ? $filter('date')(new Date(), 'yyyy-MM-dd') :$filter('date')( $scope.asOfDate, 'yyyy-MM-dd') } }).then( function(response) {
                        $scope.availableBandwidth = response.data;
                    });
                };
            });

    angular.module('okrTable', ['dataGrid', 'pagination'])
            .controller('okrController', function ($scope, $http, $filter) {
                $scope.gridOptions = {
                    data: [],
                    getData: getServerData
                };
                function getServerData(params, callback) {
                    $http.get('/planner/${planner.plan.getTeam().getName()}/${planner.plan.getQuarter()}/okr').then(function (response) {
                        var data = response.data,
                                totalItems = 1;
                        callback(data, totalItems);
                    });
                };
            });

    angular.bootstrap(document.getElementById("module2"), ['okrTable']);
</script>
</body>
</html>