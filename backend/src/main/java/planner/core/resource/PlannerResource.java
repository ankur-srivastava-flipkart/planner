package planner.core.resource;

import com.google.common.collect.Maps;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import planner.core.service.PlanningService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by kumar.vivek on 23/06/17.
 */
@Path("/planner/")
@Singleton
@Api(value = "Planner for a team")
public class PlannerResource {

  private PlanningService planningService;

  @Inject
  public PlannerResource(PlanningService planningService){
    this.planningService = planningService;
  }

  @GET
  @Path("/plan")
  @Produces("text/html")
  @ApiOperation(value = "Get the current quarter plan",
          notes = "Get the current quarter plan",
          response = String.class
  )
  public String getQuarterPlan(){
    return planningService.getPlanAsHtml();
  }

  @POST
  @Path("/{quarter}/plan/reset")
  @ApiOperation(value = "Reset Quarter",
          notes = "Reset Quarter"
  )
  @UnitOfWork
  public void resetQuarterPlan(@PathParam("quarter") String quarter){
    planningService.reset(quarter);
  }

  @GET
  @Path("/addOkr/{okr}")
  public String addOkr(@PathParam("okr") String okr){
//    Okr okr = new Okr("MPS:jir1:60:COMPLEX:1:5");
//    Okr okr1 = new Okr("GST:jir2:60:COMPLEX:1:5");
    planningService.updateOKR(okr);
    return "Added OKR";
  }

  @POST
  @Path("/action")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Various actions on the plan. ",
          notes = "Reset Quarter"
  )
  @UnitOfWork
  public String takeAction(Action action){
    LocalDate date;
    switch (action.action) {
      case FETCH_TASKS:
        if (action.param.containsKey(PARAMS.DATE) && !action.param.get(PARAMS.DATE).isEmpty()) {
          date = new LocalDate(action.param.get(PARAMS.DATE));
        } else {
          date = new LocalDate(action.param.get(PARAMS.PERIOD).split("/")[0]);
        }
        String member = action.param.containsKey(PARAMS.PERSON) && !action.param.get(PARAMS.PERSON).isEmpty() ? action.param.get(PARAMS.PERSON) : action.actor ;
       return planningService.getPlanForPersonWeek(member, date).toString();
      case INIT_QTR_PLAN:
        if (!action.actor.equals("ANKUR")) {
          return "Sorry! You are not the boss.";
        }
        planningService.reset(action.param.get(PARAMS.QUARTER));
        return "Done";
      case MODIFY_LEAVE:
        if (action.param.containsKey(PARAMS.DATE)) {
          date = new LocalDate(action.param.get(PARAMS.DATE));
          planningService.addLeave(action.actor, date, date);
        } else {
          String[] dates = action.param.get(PARAMS.PERIOD).split("/");
         planningService.addLeave(action.actor, new LocalDate(dates[0]),new LocalDate(dates[1]));
        }
        return "Added";
      case GET_QTR_PLAN: return "http://localhost:8080/planningService/plan";
      case FETCH_ONCALL:
        if (action.param.containsKey(PARAMS.DATE) && !action.param.get(PARAMS.DATE).isEmpty()) {
          date = new LocalDate(action.param.get(PARAMS.DATE));
        } else if (action.param.containsKey(PARAMS.PERIOD) && !action.param.get(PARAMS.PERIOD).isEmpty()){
          date = new LocalDate(action.param.get(PARAMS.PERIOD).split("/")[0]);
        } else {
          date = LocalDate.now();
        }
        return planningService.getOncall(date);
      case ADD_OKR:
        if (!action.actor.equals("ANKUR")) {
          return "Sorry! You are not the boss.";
        }
        planningService.updateOKR(action.param.get(PARAMS.OKR));
        return "Done";
      case GET_BANDWIDTH:
        return String.valueOf(planningService.getBandwidth());
      default: return "Action Not Supported";
    }
  }

  public enum PlanAction {
    FETCH_TASKS, INIT_QTR_PLAN, MODIFY_LEAVE, GET_QTR_PLAN, FETCH_ONCALL, ADD_OKR, GET_BANDWIDTH
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