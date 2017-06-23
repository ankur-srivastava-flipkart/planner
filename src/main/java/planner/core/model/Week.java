package planner.core.model;

import org.joda.time.LocalDate;


public class Week {
    int weekNumber;
    LocalDate startDate;
    LocalDate endDate;

    @Override
    public String toString() {
        return "Week{" +
            "weekNumber=" + weekNumber +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
