package planner.core.resource;

import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import planner.core.dto.CreateTeamRequest;
import planner.core.model.Team;
import planner.core.service.SetupService;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * Created by ankur.srivastava on 29/08/17.
 */
@Path("/team")
@Api(value = "Team")
public class TeamResource {
    private SetupService setupService;

    @Inject
    public TeamResource(SetupService setupService) {
        this.setupService = setupService;
    }

    @PUT
    @ApiOperation(value = "Create Teams",
            notes = "Create team by specifying EM, Team members and team name",
            response = Team.class
    )
    @UnitOfWork
    public Team registerPeople(CreateTeamRequest teamRequest) {
        return setupService.createTeam(teamRequest);
    }
}
