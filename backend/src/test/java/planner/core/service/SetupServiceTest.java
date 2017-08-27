package planner.core.service;

import com.google.common.collect.Lists;
import io.dropwizard.testing.junit.DAOTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import planner.core.dto.CreatePersonRequest;
import planner.core.model.Person;
import planner.core.repository.PersonRepository;
import planner.core.repository.TeamRespository;

import java.util.List;


public class SetupServiceTest {

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder().addEntityClass(Person.class).build();

    private SetupService service;

    @Before
    public void setup() {
        service = new SetupService(new PersonRepository(database.getSessionFactory()), new TeamRespository(database.getSessionFactory()));
    }

    @Test
    public void testCreatePerson() {
        List<Person> people = database.inTransaction(() -> {
             return service.createPeople(getDummyPeople());
        });

        Assert.assertNotNull(people.get(0).getId());
        Assert.assertEquals(1, people.get(0).getId().intValue());
        Assert.assertEquals("Ankur", people.get(0).getName());

    }

    private List<CreatePersonRequest> getDummyPeople() {
        return Lists.newArrayList(new CreatePersonRequest("Ankur", "abc.def"));
    }

}