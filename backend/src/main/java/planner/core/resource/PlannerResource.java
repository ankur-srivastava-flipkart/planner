package planner.core.resource;

import com.google.common.collect.Maps;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import planner.core.dto.AddOkrRequest;
import planner.core.dto.LeaveRequest;
import planner.core.dto.OkrExecutionView;
import planner.core.model.Okr;
import planner.core.service.PlanningService;
import planner.core.view.PlannerView;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by kumar.vivek on 23/06/17.
 */
@Path("/planner/{team}/{quarter}")
@Singleton
@Api(value = "Planner for a team")
public class PlannerResource {

    private PlanningService planningService;

    @Inject
    public PlannerResource(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GET
    @Path("/plan")
    @Produces("text/html")
    @ApiOperation(value = "Get the current quarter personWeeks",
            notes = "Get the current quarter personWeeks",
            response = String.class
    )
    @UnitOfWork
    public PlannerView getQuarterPlan(@PathParam("team") String team, @PathParam("quarter") String quarter) {
        return new PlannerView(planningService.getPlan(team, quarter));
    }

    @POST
    @Path("/plan/reset/")
    @ApiOperation(value = "Reset Quarter",
            notes = "Reset Quarter"
    )
    @UnitOfWork
    public void resetQuarterPlan(@PathParam("team") String team, @PathParam("quarter") String quarter,
                                 @QueryParam("startYear") int startYear, @QueryParam("endYear") int endYear) {
        planningService.reset(team, quarter, startYear, endYear);
    }

    @POST
    @Path("/addOkr")
    @UnitOfWork
    public String addOkr(@PathParam("team") String team, @PathParam("quarter") String quarter, List<AddOkrRequest> requestList) {
//    Okr okr = new Okr("MPS:jir1:60:COMPLEX:1:5");
//    Okr okr1 = new Okr("GST:jir2:60:COMPLEX:1:5");
        List<Okr> unAddedOkrs = planningService.addOkr(team, quarter, requestList.stream().map(p -> p.getOkr()).collect(Collectors.joining("*")));
        return "Added new OKRs. Already present OKRs : " + unAddedOkrs.stream().map(n -> n.toString()).collect(Collectors.joining(" * "));
    }

    @POST
    @Path("/clearPlanForOkr/{okrId}")
    @UnitOfWork
    public String removeOkr(@PathParam("team") String team, @PathParam("quarter") String quarter,  @PathParam("okrId")Integer okrId) {
       planningService.removeOkr(team, quarter,okrId);
       return "done";
    }

    @POST
    @Path("/planOkr")
    @UnitOfWork
    public String planOkr(@PathParam("team") String team, @PathParam("quarter") String quarter, List<AddOkrRequest> requestList) {
        planningService.planOkr(team, quarter, requestList);
        return "done";
    }

    @DELETE
    @Path("/plan")
    @UnitOfWork
    public String deletePlan(@PathParam("team") String team, @PathParam("quarter") String quarter) {
        planningService.deletePlan(team, quarter);
        return "done";
    }

    @POST
    @Path("/rePlan")
    @UnitOfWork
    public String rePlan(@PathParam("team") String team, @PathParam("quarter") String quarter) {
        planningService.rePlan(team, quarter);
        return "done";
    }


    @GET
    @Path("/okr")
    @UnitOfWork
    public List<OkrExecutionView> getOkr(@PathParam("team") String team, @PathParam("quarter") String quarter) {
        return planningService.getAllPlannedOKR(team, quarter);
    }

    @POST
    @Path("/addLeave")
    @UnitOfWork
    public String addLeave(@PathParam("team") String team, @PathParam("quarter") String quarter, List<LeaveRequest> requests) {
        return requests.stream().map(req -> planningService.addLeave(team, quarter, req.getActor(), req.getWeekOf(), req.getWeekOf().plusDays(req.getNumberOfDays()-1))).collect(Collectors.joining(","));
    }

    @POST
    @Path("/removeLeave")
    @UnitOfWork
    public String removeLeave(@PathParam("team") String team, @PathParam("quarter") String quarter, List<LeaveRequest> requests) {
        return requests.stream().map(req -> planningService.removeLeave(team, quarter, req.getActor(), req.getWeekOf(), req.getWeekOf().plusDays(req.getNumberOfDays()-1))).collect(Collectors.joining(","));
    }

    @POST
    @Path("/resetPlanForPerson/{actor}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Various actions on the personWeeks. ",
            notes = "Reset Quarter"
    )
    @UnitOfWork
    public String resetPlanforPerson(@PathParam("team") String team, @PathParam("quarter") String quarter, @PathParam("actor") String actor, @QueryParam("effectiveFrom") String effectiveFrom) {
        planningService.resetPlanForMember(team, quarter, actor, effectiveFrom);
        return "done";
    }


    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Various actions on the personWeeks. ",
            notes = "Reset Quarter"
    )
    @UnitOfWork
    public String takeAction(@PathParam("team") String team, @PathParam("quarter") String quarter, Action action) {
        LocalDate date;
        switch (action.action) {
            case FETCH_TASKS:
                if (action.param.containsKey(PARAMS.DATE) && !action.param.get(PARAMS.DATE).isEmpty()) {
                    date = new LocalDate(action.param.get(PARAMS.DATE));
                } else {
                    date = new LocalDate(action.param.get(PARAMS.PERIOD).split("/")[0]);
                }
                String member = action.param.containsKey(PARAMS.PERSON) && !action.param.get(PARAMS.PERSON).isEmpty() ? action.param.get(PARAMS.PERSON) : action.actor;
                return planningService.getPlanForPersonWeek(team, quarter, member, date).toString();
            case GET_QTR_PLAN:
                return "http://10.85.250.122:35432/planner/" + team + "/" + quarter + "/plan";
            case FETCH_ONCALL:
                if (action.param.containsKey(PARAMS.DATE) && !action.param.get(PARAMS.DATE).isEmpty()) {
                    date = new LocalDate(action.param.get(PARAMS.DATE));
                } else if (action.param.containsKey(PARAMS.PERIOD) && !action.param.get(PARAMS.PERIOD).isEmpty()) {
                    date = new LocalDate(action.param.get(PARAMS.PERIOD).split("/")[0]);
                } else {
                    date = LocalDate.now();
                }
                return planningService.getOncall(team, quarter, date);
            case ADD_OKR:
                if (!action.actor.equals("ANKUR")) {
                    return "Sorry! You are not the boss.";
                }
                planningService.addOkr(team, quarter, action.param.get(PARAMS.OKR));
                return "Done";
            case GET_OKR:
                planningService.getAllOKR(team, quarter);
                return "Done";
            case REMOVE_OKR:
                planningService.removeOkr(team, quarter, Integer.valueOf(action.param.get(PARAMS.OKR)));
                return "Done";
            case GET_BANDWIDTH:
                return String.valueOf(planningService.getBandwidth(team, quarter));
            case ADD_DEVOPS:
                planningService.addDevOps(team, quarter);
                return "done";
            case ADD_ONCALL:
                planningService.populateOncall(team, quarter);
                return "done";
            case GET_REMAINING_BANDWIDTH:
                LocalDate startDate = action.param.get(PARAMS.DATE) != null ? new LocalDate(action.param.get(PARAMS.DATE)) : new LocalDate();
                return String.valueOf(planningService.getRemainingBandwidth(team, quarter, startDate));
            default:
                return "Action Not Supported";
        }
    }

    public enum PlanAction {
        FETCH_TASKS, INIT_QTR_PLAN, GET_QTR_PLAN, FETCH_ONCALL, ADD_OKR, RESET_PLAN_FOR_PERSON, ADD_LEAVE, REMOVE_LEAVE, GET_REMAINING_BANDWIDTH, GET_OKR, ADD_DEVOPS, ADD_ONCALL, REMOVE_OKR, GET_BANDWIDTH
    }

    public enum PARAMS {
        DATE, PERIOD, PERSON, QUARTER, OKR
    }

    public static class Action {
        public PlanAction action;
        public String actor;
        public Map<PARAMS, String> param = Maps.newHashMap();

        public Action(PlanAction action, String actor, Map<PARAMS, String> param) {
            this.action = action;
            this.actor = actor;
            this.param = param;
        }

        public Action() {
        }

        public PlanAction getAction() {
            return action;
        }

        public void setAction(PlanAction action) {
            this.action = action;
        }

        public String getActor() {
            return actor;
        }

        public void setActor(String actor) {
            this.actor = actor;
        }

        public Map<PARAMS, String> getParam() {
            return param;
        }

        public void setParam(Map<PARAMS, String> param) {
            this.param = param;
        }
    }

}