package planner.core.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 23/06/17.
 */

@Entity
@Data
public class PersonWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @OneToOne(fetch = FetchType.EAGER)
    Person person;

    @OneToOne(fetch = FetchType.EAGER)
    Week week;

    String description = "";
    int leaves = 0;

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    List<OkrAllocation> okrAllocations = new ArrayList<>();

    @Override
    public String toString() {
        return "person=" + person +
            ", description='" + description + '\'' +
            ", leaves=" + leaves +
            ", okrList=" + getOkrList();
    }

    public String getDescriptionWithLeaves() {
        return String.format("%s:(%d):(%f):(%s)", this.description, this.leaves, this.occupied(), getOkrList().stream().map(e -> e.description + " - " + e.willSpill).reduce("",(a,b) -> a+ " ^ " +b));
    }

    public String getPrettyHtmlDescription() {
        String text = "L:" + leaves + ", O:" + occupied() + ", </br>";
        if(description.length() > 0) {
            text += "D:" + description + ", </br>";
        }
        if(!getOkrList().isEmpty()){
            text += getOkrList().stream().map(e -> e.description + " and slip = " + e.willSpill).reduce("", (a,b) -> a + "O:" + b + ", ");
        }
        return text.trim();
    }

    public float occupied() {
        return (float)okrAllocations.stream().mapToDouble( a -> a.getDaysAllocated()).sum();
    }
    public float getOccupied() {
        return occupied();
    }

    public float unoccupied() {
        return 5 - leaves - occupied();
    }

    public boolean hasLeaves () {
        return leaves > 0;
    }

    public float getAvailableBandWidth() {
        return unoccupied() * person.productivity;
    }

    public boolean isCurrentWeek() {
        return this.week.getStartDate().getWeekOfWeekyear() == LocalDate.now().getWeekOfWeekyear();
    }

    public List<Okr> getOkrList() {
        return okrAllocations.stream().map(p-> p.getOkr()).collect(Collectors.toList());
    }

    public boolean isOncall() {
        return getOkrAllocations().stream()
                .anyMatch(p -> StringUtils.equalsIgnoreCase(p.getOkr().getDescription(),"ONCALL"));
    }

    public Okr getOncallOkr() {
        return isOncall() ? getOkrAllocations().stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getOkr().getDescription(),"ONCALL")).findFirst().get().getOkr() : null;
    }
}
