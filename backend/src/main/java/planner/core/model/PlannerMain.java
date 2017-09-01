package planner.core.model;

public class PlannerMain {

  public static void main(String[] args) {
    Planner planner = new Planner("JFM" , null);
    //planner.addLeave(TeamMember.SHIKHAR, new LocalDate("2017-02-03"), new LocalDate("2017-02-10"));
   // planner.addLeave(Person.SHIKHAR, new LocalDate("2016-12-30"), new LocalDate("2017-01-05"));
   // planner.addLeave(TeamMember.AMIT, new LocalDate("2016-12-30"), new LocalDate("2017-01-05"));
    planner.printPlan();
    System.out.println(planner.getPlanAsHtml());
  }
}
