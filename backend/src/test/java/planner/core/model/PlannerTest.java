package planner.core.model;

import com.google.common.collect.Lists;
import io.dropwizard.testing.junit.DAOTestRule;
import org.junit.*;
import planner.core.dto.CreatePersonRequest;
import planner.core.dto.CreateTeamRequest;
import planner.core.repository.PersonRepository;
import planner.core.repository.TeamRespository;
import planner.core.service.SetupService;

import java.util.List;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public class PlannerTest {

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder().addEntityClass(Person.class).addEntityClass(Team.class).addEntityClass(Okr.class).build();
    private List<CreatePersonRequest> getDummyPeople() {
        return Lists.newArrayList(
                new CreatePersonRequest("Ankur", "abc.def" , 0.3f, Level.SDE3),
                new CreatePersonRequest("megha", "abc1.def1", 0.8f, Level.SDE1),
                new CreatePersonRequest("kiran", "abc2.def2", 0.6f, Level.PSE2),
                new CreatePersonRequest("amit", "abc3.def2", 0.6f, Level.SDE2),
                new CreatePersonRequest("shikhar", "abc4.def2", 0.6f, Level.SDE2),
                new CreatePersonRequest("vivek", "abc5.def2", 0.6f, Level.SDE2),
                new CreatePersonRequest("tejeswar", "abc6.def2", 0.6f, Level.SDE2),
                new CreatePersonRequest("deepak", "abc7.def2", 0.6f, Level.PSE1),
                new CreatePersonRequest("nandha", "abc8.def2", 0.6f, Level.SDE1),
                new CreatePersonRequest("shridhar", "abc9.def2", 0.6f, Level.PSE2)
        );
    }

    Team team;

    @Before
    public void setUp() throws Exception {

        SetupService service = new SetupService(new PersonRepository(database.getSessionFactory()), new TeamRespository(database.getSessionFactory()));

        CreateTeamRequest createTeamRequest = new CreateTeamRequest();
        createTeamRequest.setTeamName("OFF");
        createTeamRequest.setEm("ankur");
        createTeamRequest.setTeamMembers(Lists.newArrayList("Ankur","megha","kiran","shikhar", "nandha", "vivek", "tejeswar", "shridhar", "amit", "deepak"));
        team = database.inTransaction(() -> {
            service.createPeople(getDummyPeople());
            return service.createTeam(createTeamRequest);

        });
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWeeks() {
        new Planner("AMJ", team);
    }

    @Test
    public void testBlockPeople() {

        Planner amj = new Planner("AMJ", team);

        Okr okr = new Okr("okr1:jir1:20:COMPLEX:1:3");


        amj.blockPeople(Level.SDE2, okr, false);
        amj.printPlan();

    }


    @Test
    public void testBlockPeople1() {

        Planner amj = new Planner("AMJ", team);

        Okr okr = new Okr("okr1:jir1:400:COMPLEX:1:3");


       Assert.assertTrue(amj.blockPeople(Level.SDE2, okr, false));
        amj.printPlan();

    }

    @Test
    public void testBlockPeople2() {

        Planner amj = new Planner("AMJ", team);

        Okr okr = new Okr("MPS:jir1:60:COMPLEX:1:10");
        Okr okr1 = new Okr("GST:jir1:60:COMPLEX:1:10");
        amj.printPlan();

        amj.blockPeople(Level.SDE2, okr, false);

        amj.printPlan();

        amj.blockPeople(Level.PSE1, okr1, false);
        amj.printPlan();

        System.out.println(amj.getPlanAsHtml());

    }

    @Test
    public void testBlockPeople3() {

        Planner amj = new Planner("AMJ", team);

        Okr okr = new Okr("okr:jir1:10:COMPLEX:1:1");

        amj.blockPeople(Level.SDE3, okr, false);

        amj.printPlan();

    }

}