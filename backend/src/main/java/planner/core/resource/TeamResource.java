package planner.core.resource;

import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import planner.core.dto.CreateTeamRequest;
import planner.core.model.Team;
import planner.core.service.SetupService;

import javax.ws.rs.*;
import java.util.List;

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
    public Team registerTeam(CreateTeamRequest teamRequest) {
        return setupService.createTeam(teamRequest);
    }

    @GET
    @ApiOperation(value = "Get Teams",
            notes = "Get all teams",
            response = Team.class,
            responseContainer = "List"
    )
    @UnitOfWork
    public List<Team> getAllTeams() {
        return setupService.getAllTeams();
    }

    @GET
    @ApiOperation(value = "Get Team By Name",
            notes = "Get team by Name",
            response = Team.class,
            responseContainer = "List"
    )
    @UnitOfWork
    @Path("/{name}")
    public List<Team> getTeamByName(@PathParam("name") String name) {
        return setupService.getTeamByName(name);
    }

    @POST
    @ApiOperation(value = "Add Team member to team",
            notes = "Add team member to team",
            response = Team.class,
            responseContainer = "List"
    )
    @UnitOfWork
    @Path("/{teamName}/members/{personName}")
    public Team addTeamMember(@PathParam("teamName") String teamName, @PathParam("personName") String personName) {
        Team team = setupService.addTeamMember(teamName, personName);
        return team;
    }

    @DELETE
    @ApiOperation(value = "Remove Team member to team",
            notes = "Remove team member to team",
            response = Team.class,
            responseContainer = "List"
    )
    @UnitOfWork
    @Path("/{teamName}/members/{personName}")
    public Team removeTeamMember(@PathParam("teamName") String teamName, @PathParam("personName") String personName) {
        return setupService.removeTeamMember(teamName, personName);
    }

}
