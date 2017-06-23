package planner.core.model;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created by kumar.vivek on 23/06/17.
 */
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

}