package planner.core.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankur.srivastava on 23/06/17.
 */

public class PersonWeek {
    TeamMember person;
    Week week;
    String description = "";
    int leaves = 0;
    int occupied = 0;
    List<Okr> okrList = new ArrayList<>();

    public String getDescriptionWithLeaves() {
        return String.format("%s:(%d):(%d):(%s)", this.description, this.leaves, this.occupied, okrList.stream().map(e -> e.description + " - " + e.willSpill).reduce("",(a,b) -> a+ " ^ " +b));
    }

    public int unoccupied() {
        return 5 - leaves - occupied;
    }

}
