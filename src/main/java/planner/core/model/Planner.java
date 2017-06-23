package planner.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public class Planner {

    public static final String ONCALL = "Oncall";
    ArrayList<PersonWeek> plan= new ArrayList<>();
    List<Week> weeks = new ArrayList<>();


    public Planner(String quarter) {
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
        printPlan();
    }

    public void updateOKR(String okrs) {
        List<Okr> okrList = Arrays.stream(okrs.split("\\*"))
                .map(e -> new Okr(e))
                .sorted(new Comparator<Okr>() {
                    @Override
                    public int compare(Okr o1, Okr o2) {
                        return o2.priority - o1.priority;
                    }
                })
                .collect(Collectors.toList());

    }

    private void populateOncall() {
        int i = 0;
        for (Week week : weeks) {
            final int tempV = i;
            System.out.println(tempV % TeamMember.values().length + 1);
            List<PersonWeek> matchedWeeks = plan.stream().filter(pw ->
                    pw.week.weekNumber == week.weekNumber &&
                            pw.person == TeamMember.values()[tempV % TeamMember.values().length]
            ).collect(Collectors.toList());
            if (matchedWeeks.size() > 1) {
                System.out.println(matchedWeeks);
                throw new RuntimeException("More than one weeks matched");
            }
            matchedWeeks.get(0).description = "Oncall";
            i = i+1;
        }
    }

    public void printPlan() {
        System.out.print("Employee/Week");
        for (Week week : weeks) {
            System.out.print("," + week.startDate  + " : " + week.endDate );
        }
        System.out.println();
        for (TeamMember member : TeamMember.values()) {
            System.out.print(member.name() + ",");
            String row = plan.stream().filter(pw -> pw.person == member)
                    .sorted((o1, o2) -> (o2.week.weekNumber - o1.week.weekNumber))
                    .map(pw -> pw.description)
                    .reduce("", (a, b) -> a + "," + b);
            System.out.print(row);
            System.out.println();
        }
    }

    private PersonWeek getPersonWeek(Week week, final TeamMember teamMember) {
        final int weekNumber = week.weekNumber;
        return null;
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
            weeks.add(e);
            System.out.println(startDate.withDayOfWeek(DateTimeConstants.MONDAY) + " - " + startDate.withDayOfWeek(DateTimeConstants.SUNDAY));
            startDate = startDate.plusDays(7);
            weekNumber +=1;
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

    private void addLeave(TeamMember teamMember, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        List<PersonWeek> weeksOfInterest = plan.stream()
            .filter(personWeek -> personWeek.person == teamMember)
            .filter(personWeek -> !personWeek.week.startDate.isAfter(leaveEndDate)
                || !personWeek.week.endDate.isBefore(leaveStartDate))
            .collect(Collectors.toList());
        for (PersonWeek personWeek : weeksOfInterest) {
            if (personWeek.description.contains(ONCALL)) {
                swapOncall(personWeek);
            }
            LocalDate leaveStartInWeek = leaveStartDate.isAfter(personWeek.week.startDate) ? leaveStartDate : personWeek.week.startDate;
            LocalDate leaveEndInWeek = leaveEndDate.isBefore(personWeek.week.endDate) ? leaveEndDate : personWeek.week.endDate;
            int noOfLeaveDays = Days.daysBetween(leaveStartInWeek, leaveEndInWeek).getDays() + 1;
            personWeek.leaves += noOfLeaveDays;
        }
    }

    private void swapOncall(PersonWeek personWeek) {
        try {
            PersonWeek candidateForOncallSwap = getPlanForWeek(personWeek.week.startDate)
                .stream()
                .filter(pw -> pw.leaves == 0)
                .findAny()
                .get();
            personWeek.description = "";
            candidateForOncallSwap.description = ONCALL;
            PersonWeek oncallWeek = getPlanForCandidate(candidateForOncallSwap.person)
                .stream()
                .filter(pw -> pw.description.contains(ONCALL))
                .findFirst().get();
            oncallWeek.description = "";
            getPlanForCandidate(personWeek.person)
                .stream()
                .filter(pw -> pw.week.weekNumber == oncallWeek.week.weekNumber)
                .findAny()
                .get().description = ONCALL;
        } catch (Exception e) {
            System.out.println("ERROR : Could not swap oncall");
        }
    }

    private List<PersonWeek> getPlanForWeek(LocalDate date) {
        return plan.stream()
            .filter(pw -> !pw.week.startDate.isAfter(date))
            .filter(pw -> !pw.week.endDate.isBefore(date))
            .collect(Collectors.toList());
    }

    private List<PersonWeek> getPlanForCandidate(TeamMember teamMember) {
        return plan.stream()
            .filter(pw -> pw.person == teamMember)
            .collect(Collectors.toList());
    }
}
