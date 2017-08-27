package planner.core.resource;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.time.LocalDate;
import planner.core.model.Planner;
import planner.core.model.TeamMember;

/**
 * Created by kumar.vivek on 23/06/17.
 */
@Path("/planner")
@Singleton
public class PlannerResource {

  private Planner planner;

  @Inject
  public PlannerResource(Planner planner){
    this.planner = planner;
  }

  @GET
  @Path("/plan")
  @Produces("text/html")
  public String getQuarterPlan(){
    return planner.getPlanAsHtml();
  }

  @POST
  @Path("/{quarter}/plan/reset")
  public void resetQuarterPlan(@PathParam("quarter") String quarter){
    planner.reset(quarter);
  }

  @GET
  @Path("/addOkr/{okr}")
  public String addOkr(@PathParam("okr") String okr){
//    Okr okr = new Okr("MPS:jir1:60:COMPLEX:1:5");
//    Okr okr1 = new Okr("GST:jir2:60:COMPLEX:1:5");
    planner.updateOKR(okr);
    return "Added OKR";
  }

  @POST
  @Path("/action")
  @Consumes(MediaType.APPLICATION_JSON)
  public String takeAction(Action action){
    LocalDate date;
    switch (action.action) {
      case "fetch_tasks":
        if (action.param.containsKey("date") && !action.param.get("date").isEmpty()) {
          date = new LocalDate(action.param.get("date"));
        } else {
          date = new LocalDate(action.param.get("period").split("/")[0]);
        }
        String member = action.param.containsKey("person") && !action.param.get("person").isEmpty() ? action.param.get("person") : action.actor ;
        TeamMember teamMember = TeamMember.valueOf(member);
        return planner.getPlanForPersonWeek(teamMember, date).toString();
      case "init_qtr_plan":
        if (!action.actor.equals("ANKUR")) {
          return "Sorry! You are not the boss.";
        }
        planner.reset(action.param.get("quarter"));
        return "Done";
      case "modify_leave":
        if (action.param.containsKey("date")) {
          date = new LocalDate(action.param.get("date"));
          planner.addLeave(TeamMember.valueOf(action.actor), date, date);
        } else {
          String[] dates = action.param.get("period").split("/");
          planner.addLeave(TeamMember.valueOf(action.actor), new LocalDate(dates[0]),
              new LocalDate(dates[1]));
        }
        return "Added";
      case "modify_oncall": break;
      case "get_qtr_plan": return "http://172.20.160.123:8080/planner/plan";
      case "fetch_oncall":
        if (action.param.containsKey("date") && !action.param.get("date").isEmpty()) {
          date = new LocalDate(action.param.get("date"));
        } else if (action.param.containsKey("period") && !action.param.get("period").isEmpty()){
          date = new LocalDate(action.param.get("period").split("/")[0]);
        } else {
          date = LocalDate.now();
        }
        return planner.getOncall(date);
      case "add_okr":
        if (!action.actor.equals("ANKUR")) {
          return "Sorry! You are not the boss.";
        }
        planner.updateOKR(action.param.get("okr"));
        return "Done";
      case "get_bandwidth":
        return String.valueOf(planner.getBandwidth());
      default: return "Action Not Supported";
    }
    return null;
  }


  public static class Action {
    public String action;
    public String actor;
    public Map<String, String> param = Maps.newHashMap();

    public Action(String action, String actor, Map<String, String> param) {
      this.action = action;
      this.actor = actor;
      this.param = param;
    }

    public Action() {
    }

    public String getAction() {
      return action;
    }

    public void setAction(String action) {
      this.action = action;
    }

    public String getActor() {
      return actor;
    }

    public void setActor(String actor) {
      this.actor = actor;
    }

    public Map<String, String> getParam() {
      return param;
    }

    public void setParam(Map<String, String> param) {
      this.param = param;
    }
  }

}