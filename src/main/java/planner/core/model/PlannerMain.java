package planner.core.model;

import org.joda.time.LocalDate;

public class PlannerMain {

  public static void main(String[] args) {
    Planner planner = new Planner("JFM");
    //planner.addLeave(TeamMember.SHIKHAR, new LocalDate("2017-02-03"), new LocalDate("2017-02-10"));
    planner.addLeave(TeamMember.SHIKHAR, new LocalDate("2016-12-30"), new LocalDate("2017-01-05"));
    planner.addLeave(TeamMember.AMIT, new LocalDate("2016-12-30"), new LocalDate("2017-01-05"));
    planner.printPlan();
  }
}
