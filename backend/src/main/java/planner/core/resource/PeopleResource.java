package planner.core.resource;

import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import planner.core.dto.CreatePersonRequest;
import planner.core.model.Person;
import planner.core.service.SetupService;

import javax.ws.rs.*;
import java.util.List;

/**
 * Created by ankur.srivastava on 27/08/17.
 */
@Path("/people")
@Api(value = "People")
public class PeopleResource {

    private SetupService setupService;

    @Inject
    public PeopleResource(SetupService setupService) {
        this.setupService = setupService;
    }

    @PUT
    @ApiOperation(value = "Register people involved in the app",
            notes = "All Actors (EM, Team members) need to be registered using this API",
            response = Person.class,
            responseContainer = "List"
    )
    @UnitOfWork
    public List<Person> registerPeople(List<CreatePersonRequest> people) {
        return setupService.createPeople(people);
    }

    @POST
    @ApiOperation(value = "Update Person Information",
            notes = "Update person details",
            response = Person.class
    )
    @UnitOfWork
    @Path("/{name}")
    public Person updatePersonInformation(@PathParam("name") String personName, CreatePersonRequest personRequest) {
        return setupService.updatePerson(personName, personRequest);
    }

    @GET
    @ApiOperation(value = "Get people registered in the app",
            notes = "All Actors (EM, Team members) need to be registered using PUT API",
            response = Person.class,
            responseContainer = "List"
    )
    @UnitOfWork
    public List<Person> getAllRegisteredPeople() {
        return setupService.getAllPeople();
    }
}
