<html>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.0/angular.min.js"></script>
<link rel="styleSheet" href="/planner-ui/uigrid/ui-grid.min.css"/>
<script src="/planner-ui/uigrid/ui-grid.min.js"> </script>

<style type="text/css">
    td {border:1px solid grey; font-size: x-small}
    th {border:1px solid grey; font-size: x-small}
    p {font-size: x-small}
    td {font-size: x-small}

    .oncall { background-color: LightPink;}
    .leaves { background-color: LightSkyBlue ;}
    .current { background-color: #DDDDDD;}
    .spillOver {background-color: #ff6060;}

    .grid {
        width: 80%;
        height: 100px;
    }

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
                        <#if pw.leaves gt 0 >
                            Leaves : <b>${pw.leaves}</b>
                        </#if>
                    <#if pw.okrList?has_content>
                        <#list pw.okrAllocations as okrAllocation>
                            <li>${okrAllocation.okr.description}(${okrAllocation.daysAllocated})</li>
                        </#list>
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
    <div ui-grid="gridOptions" class="grid"></div>
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

    angular.module('okrTable' , ['ui.grid'])
            .controller('okrController', ['$scope', '$http', 'uiGridConstants', function ($scope, $http, uiGridConstants) {
                $scope.gridOptions = {
                    columnDefs: [
                        {field: 'okr.id' , name: 'Id', width:"4%"},
                        {field: 'okr.description', name: 'Description', width:"21%"},
                        {field: 'okr.jiraEpic', name:'Jira', width:"10%"},
                        {field: 'okr.effortinPersonDays', name:'Effort(PD)', width:"5%"},
                        {field: 'okr.complexity',name:'Complexity', width:"10%"},
                        {field: 'okr.priority',name:'Priority', width:"10%"},
                        {field: 'okr.parallelism',name:'Parallelism', width:"10%"},
                        {field: 'startDate',name:'Start Date', width:"10%", cellFilter: 'date:longdate'},
                        {field: 'endDate',name:'End Date', width:"10%" , cellFilter: 'date:longdate'},
                        {field: 'okr.spillOver',name:'SpillOver(PD)', width:"10%" }
                        ]
                };

                $http.get('/planner/${planner.plan.getTeam().getName()}/${planner.plan.getQuarter()}/okr')
                        .then(function(response) {$scope.gridOptions.data = response.data;});
            }]);

    angular.bootstrap(document.getElementById("module2"), ['okrTable']);
</script>
</body>
</html>