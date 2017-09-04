package planner.core.model;

import lombok.Data;
import org.joda.time.LocalDate;

import javax.persistence.*;

@Entity
@Data
@NamedQueries({
        @NamedQuery(name = "planner.core.model.week.findByQuarter",
                query = "select p from Okr p where p.quarter like :quarter")
})
public class Week {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    private String quarter;
    private int weekNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    public String toString() {
        return "Week{" +
            "weekNumber=" + weekNumber +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
