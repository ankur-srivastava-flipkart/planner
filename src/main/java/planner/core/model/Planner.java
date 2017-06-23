package planner.core.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
@Singleton
public class Planner {

    public static final String ONCALL = "Oncall";
    ArrayList<PersonWeek> plan= new ArrayList<>();
    List<Week> weeks = new ArrayList<>();

    int status = 0;


    public Planner(String quarter) {
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
        //printPlan();
    }

    public void reset(String quarter){
        plan = new ArrayList<>();
        weeks = new ArrayList<>();
        status = 0;
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
        //printPlan();
    }

    public void updateOKR(String okrs) {
        List<Okr> okrList = Arrays.stream(okrs.split("\\*"))
                .map(Okr::new)
                .sorted((o1, o2) -> o2.priority - o1.priority)
                .collect(Collectors.toList());

        for (Okr eachOkr : okrList) {
            if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays <=3) {
                eachOkr.willSpill = blockPeople(Level.PSE2, eachOkr, true);
            } else if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays <=10) {
                eachOkr.willSpill = blockPeople(Level.SDE1, eachOkr, true);
            } else if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays >10) {
                eachOkr.willSpill = blockPeople(Level.SDE1, eachOkr, false);
            } else if (eachOkr.complexity == Complexity.MEDIUM && eachOkr.effortinPersonDays <=10) {
                eachOkr.willSpill = blockPeople(Level.SDE1, eachOkr, true);
            } else if (eachOkr.complexity == Complexity.MEDIUM && eachOkr.effortinPersonDays >10) {
                eachOkr.willSpill = blockPeople(Level.SDE1, eachOkr, false);
            } else if (eachOkr.complexity == Complexity.COMPLEX && eachOkr.effortinPersonDays <=10) {
                eachOkr.willSpill = blockPeople(Level.SDE2, eachOkr, true);
            }  else if (eachOkr.complexity == Complexity.COMPLEX && eachOkr.effortinPersonDays > 10) {
                eachOkr.willSpill = blockPeople(Level.SDE2, eachOkr, false);
            }

        }
    }

    public boolean blockPeople(Level levelOnwards, Okr okr, boolean allSameLevelPossibleBestEffort) {

        List<List<TeamMember>> allCombinations = new ArrayList<>();

        List<TeamMember> values = Arrays.stream(TeamMember.values()).filter(e -> e.level.ordinal() >= levelOnwards.ordinal()).collect(Collectors.toList());


        for (int i = 1 ; i <= okr.parallelism ; i++) {
            allCombinations.addAll( Permute.getAllCombinations(values.toArray(new TeamMember[values.size()]),values.size(), i));
        }


        Map<List<TeamMember>, LocalDate> endDateMap = new HashMap<>();
        for(List<TeamMember> eachCombination : allCombinations) {
            int effortRemaining = okr.effortinPersonDays;
            Week lastWeek = null;
            for (Week week : weeks) {
                double totalWeekEffort = 0;

                for (TeamMember member : eachCombination) {
                    PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.startDate);
                    totalWeekEffort += (5 - planForPersonWeek.occupied - planForPersonWeek.leaves)*member.productivity;
                }
                effortRemaining -= Math.round(totalWeekEffort);
                System.out.println(week.weekNumber + " , " + effortRemaining + " - " + totalWeekEffort);

                if (effortRemaining <= 0) {
                    endDateMap.put(eachCombination,week.endDate);
                    break;
                }
                lastWeek = week;
            }

            if (effortRemaining > 0) {
                endDateMap.put(eachCombination,lastWeek.endDate.plusDays(effortRemaining/eachCombination.size()));
            }

        }

        Map.Entry<List<TeamMember>, LocalDate> listLocalDateEntry = endDateMap.entrySet().stream().sorted(new Comparator<Map.Entry<List<TeamMember>, LocalDate>>() {
            @Override
            public int compare(Map.Entry<List<TeamMember>, LocalDate> o1, Map.Entry<List<TeamMember>, LocalDate> o2) {

                return o1.getValue().compareTo(o2.getValue());
            }
        }).findFirst().get();

        System.out.println(listLocalDateEntry.getKey());

        double efforRemaining = okr.effortinPersonDays;

        for (Week week : weeks) {
            for (TeamMember member :  listLocalDateEntry.getKey()) {
                PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.startDate);
                if(planForPersonWeek.unoccupied() == 0 ) {
                    continue;
                }
                if (efforRemaining >= planForPersonWeek.unoccupied() * member.productivity) {
                    efforRemaining -= planForPersonWeek.unoccupied() * member.productivity;
                    planForPersonWeek.occupied += planForPersonWeek.unoccupied();
                    planForPersonWeek.okrList.add(okr);
                } else {
                    planForPersonWeek.occupied += Math.ceil(efforRemaining / member.productivity);
                    efforRemaining -= efforRemaining;
                    planForPersonWeek.okrList.add(okr);
                }
                if ((int)efforRemaining == 0) {
                    break;
                }
            }
            if ((int)efforRemaining == 0) {
                break;
            }
        }

        if (efforRemaining > 0) {
            return true;
        }
        return false;
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
            matchedWeeks.get(0).occupied = 5 - matchedWeeks.get(0).leaves;
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
            String row = plan.stream().filter(pw -> pw.person == member)
                    .sorted((o1, o2) -> (o1.week.weekNumber - o2.week.weekNumber))
                    .map(PersonWeek::getDescriptionWithLeaves)
                    .reduce(member.name(), (a, b) -> a + "," + b);
            System.out.print(row);
            System.out.println();
        }
    }

    public String getPlanAsHtml() {
        String html = "<html>";
        html += "<style type=\"text/css\">td {border:1px solid grey;} th {border:1px solid grey;} table {border:1px solid grey;}</style>";
        html += "<body>";
        html += "<table>";
        html += "<tr>";
        html += "<th>Employee/Week</th>";
        for (Week week : weeks) {
            html += "<th>" + week.startDate  + " : " + week.endDate + "</th>";
        }
        html += "</tr>";
        for (TeamMember member : TeamMember.values()) {
            String row = plan.stream().filter(pw -> pw.person == member)
                .sorted((o1, o2) -> (o1.week.weekNumber - o2.week.weekNumber))
                .map(PersonWeek::getPrettyHtmlDescription)
                .reduce("<td>" + member.name() + "</td>", (a, b) -> a + "<td>" + b + "</td>");
            html += "<tr>" + row + "</tr>";
        }
        html += "</table>";
        html += "<body>";
        html += "</html>";
        return html;
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

    public void addLeave(TeamMember teamMember, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        List<PersonWeek> weeksOfInterest = plan.stream()
            .filter(personWeek -> personWeek.person == teamMember)
            .filter(personWeek -> isOverlappingWithLeaves(personWeek.week, leaveStartDate, leaveEndDate))
            .collect(Collectors.toList());
        for (PersonWeek personWeek : weeksOfInterest) {
            if (personWeek.description.contains(ONCALL)) {
                swapOncall(personWeek);
            }
            long noOfLeaveDays = 0;
            LocalDate leaveStartInWeek = leaveStartDate.isAfter(personWeek.week.startDate) ? leaveStartDate : personWeek.week.startDate;
            LocalDate leaveEndInWeek = leaveEndDate.isBefore(personWeek.week.endDate) ? leaveEndDate : personWeek.week.endDate;
            for (LocalDate date = leaveStartInWeek; !date.isAfter(leaveEndInWeek); date = date.plusDays(1)) {
                if (date.toDateTimeAtCurrentTime().getDayOfWeek() <= 5) {
                    noOfLeaveDays++;
                }
            }
            personWeek.leaves += noOfLeaveDays;
        }
    }

    private boolean isOverlappingWithLeaves(Week week, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        return (!week.startDate.isAfter(leaveStartDate) && !week.endDate.isBefore(leaveEndDate))
            || (!week.startDate.isBefore(leaveStartDate) && !week.startDate.isAfter(leaveEndDate))
            || (!week.endDate.isBefore(leaveStartDate) && !week.endDate.isAfter(leaveEndDate));
    }

    private void swapOncall(PersonWeek requesterCurrentWeek) {
        try {
            List<PersonWeek> requesteeCurrentWeeks = getPlanForWeek(requesterCurrentWeek.week.startDate)
                .stream()
                .filter(pw -> pw.leaves == 0)
                .filter(pw -> pw.person != requesterCurrentWeek.person)
                .collect(Collectors.toList());
            for (PersonWeek requesteeCurrentWeek : requesteeCurrentWeeks) {
                PersonWeek requesteeOncallWeek = getPlanForTeamMember(requesteeCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.description.contains(ONCALL))
                    .findFirst().get();
                PersonWeek requesterOncallWeek = getPlanForTeamMember(requesterCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.week.weekNumber == requesteeOncallWeek.week.weekNumber)
                    .findAny()
                    .get();
                if (requesterOncallWeek.leaves == 0) {
                    requesterOncallWeek.description = ONCALL;
                    requesterOncallWeek.occupied = 5;
                    requesteeOncallWeek.description = "";
                    requesteeOncallWeek.occupied = 0;
                    requesterCurrentWeek.description = "";
                    requesterCurrentWeek.occupied = 0;
                    requesteeCurrentWeek.description = ONCALL;
                    requesteeCurrentWeek.occupied = 5;
                    break;
                }
            }

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

    private PersonWeek getPlanForPersonWeek(TeamMember teamMember, LocalDate date) {
        return plan.stream()
                .filter(pw -> pw.person == teamMember)
                .filter(pw -> !pw.week.startDate.isAfter(date))
                .filter(pw -> !pw.week.endDate.isBefore(date))
                .findFirst()
                .get();
    }

    private List<PersonWeek> getPlanForTeamMember(TeamMember teamMember) {
        return plan.stream()
            .filter(pw -> pw.person == teamMember)
            .collect(Collectors.toList());
    }
}
