package planner.core.model;

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

    @Override
    public String toString() {
        return "person=" + person +
            ", description='" + description + '\'' +
            ", leaves=" + leaves +
            ", okrList=" + okrList;
    }

    public String getDescriptionWithLeaves() {
        return String.format("%s:(%d):(%d):(%s)", this.description, this.leaves, this.occupied, okrList.stream().map(e -> e.description + " - " + e.willSpill).reduce("",(a,b) -> a+ " ^ " +b));
    }

    public String getPrettyHtmlDescription() {
        String text = "L:" + leaves + ", O:" + occupied + ", </br>";
        if(description.length() > 0) {
            text += "D:" + description + ", </br>";
        }
        if(!okrList.isEmpty()){
            text += okrList.stream().map(e -> e.description + " and slip = " + e.willSpill).reduce("", (a,b) -> a + "O:" + b + ", ");
        }
        return text.trim();
    }

    public int unoccupied() {
        return 5 - leaves - occupied;
    }

}
