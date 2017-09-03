package planner.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
@Singleton
@NoArgsConstructor
@Data
public class Planner {

    public static final String ONCALL = "Oncall";
    ArrayList<PersonWeek> plan= new ArrayList<>();
    List<Week> weeks = new ArrayList<>();

    int status = 0;

    Team team;


    public Planner(String quarter, Team team) {
        this.team = team;
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
        printPlan();

    }

    public void reset(String quarter){
        plan = new ArrayList<>();
        weeks = new ArrayList<>();
        status = 0;
        populateWeeks(quarter);
        populatePlan();
        populateOncall();
        printPlan();
    }

    public void updateOKR(List<Okr> okrList) {

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

        List<List<Person>> allCombinations = new ArrayList<>();

        List<Person> values = team.getTeamMember().stream().filter(e -> e.level.ordinal() >= levelOnwards.ordinal()).collect(Collectors.toList());


        for (int i = 1 ; i <= okr.parallelism ; i++) {
            allCombinations.addAll( Permute.getAllCombinations(values.toArray(new Person[values.size()]),values.size(), i));
        }


        Map<List<Person>, LocalDate> endDateMap = new HashMap<>();
        for(List<Person> eachCombination : allCombinations) {
            int effortRemaining = okr.effortinPersonDays;
            Week lastWeek = null;
            for (Week week : weeks) {
                double totalWeekEffort = 0;

                for (Person member : eachCombination) {
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

        Map.Entry<List<Person>, LocalDate> listLocalDateEntry = endDateMap.entrySet().stream().sorted(new Comparator<Map.Entry<List<Person>, LocalDate>>() {
            @Override
            public int compare(Map.Entry<List<Person>, LocalDate> o1, Map.Entry<List<Person>, LocalDate> o2) {

                return o1.getValue().compareTo(o2.getValue());
            }
        }).findFirst().get();

        System.out.println(listLocalDateEntry.getKey());

        double efforRemaining = okr.effortinPersonDays;

        for (Week week : weeks) {
            for (Person member :  listLocalDateEntry.getKey()) {
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
            System.out.println(tempV % team.getTeamMember().size() + 1);
            List<PersonWeek> matchedWeeks = plan.stream().filter(pw ->
                    pw.week.weekNumber == week.weekNumber &&
                            StringUtils.equals(pw.person.getName(),team.getTeamMember().get(tempV % team.getTeamMember().size()).getName())
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
        for (Person member : team.getTeamMember()) {
            String row = plan.stream().filter(pw -> pw.person == member)
                    .sorted((o1, o2) -> (o1.week.weekNumber - o2.week.weekNumber))
                    .map(PersonWeek::getDescriptionWithLeaves)
                    .reduce(member.getName(), (a, b) -> a + "," + b);
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
        for (Person member : team.getTeamMember()) {
            String row = plan.stream().filter(pw -> pw.person == member)
                .sorted((o1, o2) -> (o1.week.weekNumber - o2.week.weekNumber))
                .map(PersonWeek::getPrettyHtmlDescription)
                .reduce("<td>" + member.getName() + "</td>", (a, b) -> a + "<td>" + b + "</td>");
            html += "<tr>" + row + "</tr>";
        }
        html += "</table>";
        html += "<body>";
        html += "</html>";
        return html;
    }

    private void populatePlan() {
        for(Person member : team.getTeamMember()) {
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

    public void addLeave(Person person, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        List<PersonWeek> weeksOfInterest = plan.stream()
            .filter(personWeek -> StringUtils.equalsIgnoreCase(personWeek.person.getName(), person.getName()))
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

    public void swapOncall(PersonWeek requesterCurrentWeek) {
        try {
            List<PersonWeek> requesteeCurrentWeeks = getPlanForWeek(requesterCurrentWeek.week.startDate)
                .stream()
                .filter(pw -> pw.leaves == 0)
                .filter(pw -> pw.person != requesterCurrentWeek.person)
                .collect(Collectors.toList());
            for (PersonWeek requesteeCurrentWeek : requesteeCurrentWeeks) {
                PersonWeek requesteeOncallWeek = getPlanForPerson(requesteeCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.description.contains(ONCALL))
                    .findFirst().get();
                PersonWeek requesterOncallWeek = getPlanForPerson(requesterCurrentWeek.person)
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

    public List<PersonWeek> getPlanForWeek(LocalDate date) {
        return plan.stream()
            .filter(pw -> !pw.week.startDate.isAfter(date))
            .filter(pw -> !pw.week.endDate.isBefore(date))
            .collect(Collectors.toList());
    }

    public PersonWeek getPlanForPersonWeek(Person person, LocalDate date) {
        return plan.stream()
                .filter(pw -> StringUtils.equalsIgnoreCase(pw.person.getName(), person.getName()))
                .filter(pw -> !pw.week.startDate.isAfter(date))
                .filter(pw -> !pw.week.endDate.isBefore(date))
                .findFirst()
                .get();
    }

    public int getBandwidth() {
        return plan.stream()
            .mapToInt(PersonWeek::unoccupied)
            .sum();
    }

    public List<PersonWeek> getPlanForPerson(Person person) {
        return plan.stream()
            .filter(pw -> StringUtils.equalsIgnoreCase(pw.person.getName(), person.getName()))
            .collect(Collectors.toList());
    }

    public String getOncall(LocalDate date) {
        return plan.stream()
            .filter(pw -> !pw.week.startDate.isAfter(date))
            .filter(pw -> !pw.week.endDate.isBefore(date))
            .filter(pw -> pw.description.contains("Oncall"))
            .findAny()
            .get()
            .person.getName();
    }
}
