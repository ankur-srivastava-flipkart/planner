package planner.core.model;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
  @Path("/{quarter}/plan")
  @Produces("text/html")
  public String getQuarterPlan(@PathParam("quarter") String quarter){
    return planner.getPlanAsHtml();
  }

  @POST
  @Path("/{quarter}/plan/reset")
  public void resetQuarterPlan(@PathParam("quarter") String quarter){
    planner.reset(quarter);
  }

  @POST
  @Path("/action")
  public String takeAction(Action action){
    switch (action.action) {
      case "fetch_task": break;
      case "init_qtr_plan": break;
      case "modify_leave": break;
      case "modify_oncall": break;
    }
    return "Done";
  }


  public static class Action {
    String action;
    String actor;
    Map<String, String> param = Maps.newHashMap();
  }

}