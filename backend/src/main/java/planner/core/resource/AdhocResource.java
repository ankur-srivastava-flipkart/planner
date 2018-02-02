package planner.core.resource;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import planner.core.service.PlanningService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by ankur.srivastava on 02/02/18.
 */
@Path("/adhoc/{team}/{quarter}")
@Singleton
@Api(value = "Adhoc tasks for planner")
public class AdhocResource {

    private PlanningService planningService;

    @Inject
    public AdhocResource(PlanningService planningService) {
        this.planningService = planningService;
    }


    @POST
    @Path("/extendMayInJFMA")
    @UnitOfWork
    public String extendMayInJFMA(@PathParam("team") String team, @PathParam("quarter") String quarter) {
        planningService.extendMayInJFMA(team);
        return "done";
    }

}
