package planner.core.model;

import com.opencsv.CSVWriter;
import org.joda.time.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public class Planner {
    ArrayList<PersonWeek> plan= new ArrayList<>();
    List<Week> weeks = new ArrayList<>();


    public Planner(String quarter) {
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
    }

    private void populateOncall() {
        int i = 0;
        for (Week week : weeks) {
            List<PersonWeek> matchedWeeks = plan.stream().filter(pw ->
                    pw.week.weekNumber == week.weekNumber &&
                            pw.person == TeamMember.values()[i % TeamMember.values().length + 1]
            ).collect(Collectors.toList());
            if (matchedWeeks.size() > 1) {
                System.out.println(matchedWeeks);
                throw new RuntimeException("More than one weeks matched");
            }
            matchedWeeks.get(0).description = "Oncall";
        }
    }

    public void printPlan() {
        //CSVWriter writer = new CSVWriter(new StringWriter());

        //writer.writeNext(Arrays.asList("Employee/Week", weeks.stream().), true);
        System.out.print("Employee/Week");
        for (Week week : weeks) {
            System.out.print(week.startDate  + " : " + week.endDate);
        }
        System.out.println();
        for (TeamMember member : TeamMember.values()) {

        }
    }

    private PersonWeek getPersonWeek(Week week, final TeamMember teamMember) {
        final int weekNumber = week.weekNumber;

    }

    private void populatePlan() {
        for(TeamMember member : TeamMember.values()) {
            for (Week week : weeks) {
                PersonWeek personWeek = new PersonWeek();
                personWeek.person = member;
                personWeek.week = week;
                plan.add(personWeek);
            }

        }
    }

    private void populateWeeks(String quarter) {
        int startingMonth = getStartingMonth(quarter);
        LocalDate startDate = new LocalDate().withYear(2017).withMonthOfYear(startingMonth + 1).dayOfMonth().withMinimumValue();
        LocalDate endDate = new LocalDate().withYear(2017).withMonthOfYear(startingMonth + quarter.length()).dayOfMonth().withMaximumValue();

        int weekNumber =1;
        while (startDate.isBefore(endDate.plusDays(7))) {
            Week e = new Week();
            e.weekNumber = weekNumber;
            e.startDate = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
            e.endDate = startDate.withDayOfWeek(DateTimeConstants.SUNDAY);
            weeks.add(e)
            System.out.println(startDate.withDayOfWeek(DateTimeConstants.MONDAY) + " - " + startDate.withDayOfWeek(DateTimeConstants.SUNDAY));
            startDate = startDate.plusDays(7);
        }
    }

    private int getStartingMonth(String quarter) {
        if (quarter == null) {
            throw new RuntimeException("Invalid quarter");
        }

        String year = "JFMAMJJASONDJ";
        int month = year.indexOf(quarter);

        if (month == -1) {
            throw new RuntimeException("Invalid quarter");
        }
        return month;
    }
}
