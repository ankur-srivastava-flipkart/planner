package planner.core.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public class PlannerTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWeeks() {
        new Planner("AMJ");
    }

    @Test
    public void testBlockPeople() {

        Planner amj = new Planner("AMJ");

        Okr okr = new Okr("okr1:jir1:20:COMPLEX:1:3");


        amj.blockPeople(Level.SDE2, okr, false);
        amj.printPlan();

    }


    @Test
    public void testBlockPeople1() {

        Planner amj = new Planner("AMJ");

        Okr okr = new Okr("okr1:jir1:400:COMPLEX:1:3");


       Assert.assertTrue(amj.blockPeople(Level.SDE2, okr, false));
        amj.printPlan();

    }

    @Test
    public void testBlockPeople2() {

        Planner amj = new Planner("AMJ");

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

        Planner amj = new Planner("AMJ");

        Okr okr = new Okr("okr:jir1:10:COMPLEX:1:1");

        amj.blockPeople(Level.SDE3, okr, false);

        amj.printPlan();

    }

}