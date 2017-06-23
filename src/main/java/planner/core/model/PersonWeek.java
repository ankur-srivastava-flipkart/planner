package planner.core.model;

/**
 * Created by ankur.srivastava on 23/06/17.
 */

public class PersonWeek {
    TeamMember person;
    Week week;
    String description = "";
    int leaves = 0;
    int occupied = 0;

    public String getDescriptionWithLeaves() {
        return String.format("%s:(%d)", this.description, this.leaves);
    }

    public int unoccupied() {
        return 5 - leaves - occupied;
    }

}
