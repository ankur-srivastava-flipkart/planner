package planner.core.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
@Data
public class Planner {

    public static final String ONCALL = "Oncall";
    private Plan plan;

    public Planner withPlan(Plan plan) {
        this.plan = plan;
        return this;
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

        List<Person> values = plan.getTeam().getTeamMember().stream().filter(e -> e.level.ordinal() >= levelOnwards.ordinal()).collect(Collectors.toList());


        for (int i = 1 ; i <= okr.parallelism ; i++) {
            allCombinations.addAll( Permute.getAllCombinations(values.toArray(new Person[values.size()]),values.size(), i));
        }


        Map<List<Person>, LocalDate> endDateMap = new HashMap<>();
        for(List<Person> eachCombination : allCombinations) {
            int effortRemaining = okr.effortinPersonDays;
            Week lastWeek = null;
            for (Week week : plan.getWeeks()) {
                double totalWeekEffort = 0;

                for (Person member : eachCombination) {
                    PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.getStartDate());
                    totalWeekEffort += (5 - planForPersonWeek.occupied - planForPersonWeek.leaves)*member.productivity;
                }
                effortRemaining -= Math.round(totalWeekEffort);
                System.out.println(week.getWeekNumber() + " , " + effortRemaining + " - " + totalWeekEffort);

                if (effortRemaining <= 0) {
                    endDateMap.put(eachCombination,week.getEndDate());
                    break;
                }
                lastWeek = week;
            }

            if (effortRemaining > 0) {
                endDateMap.put(eachCombination,lastWeek.getEndDate().plusDays(effortRemaining/eachCombination.size()));
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

        for (Week week : plan.getWeeks()) {
            for (Person member :  listLocalDateEntry.getKey()) {
                PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.getStartDate());
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

    public void populateOncall() {
        int i = 0;
        for (Week week : plan.getWeeks()) {
            final int tempV = i;
            System.out.println(tempV % plan.getTeam().getTeamMember().size() + 1);

            List<Person> eligibleMembers = plan.getTeam().getTeamMember().stream().filter(p -> p.getLevel().ordinal() > Level.PSE3.ordinal())
                    .sorted(new Comparator<Person>() {
                        @Override
                        public int compare(Person o1, Person o2) {
                            return o1.getId().compareTo(o2.getId());
                        }
                    })
                    .collect(Collectors.toList());

            List<PersonWeek> matchedWeeks = plan.getPersonWeeks().stream().filter(pw ->
                    pw.week.getWeekNumber() == week.getWeekNumber() &&
                            StringUtils.equals(pw.person.getName(), eligibleMembers.get(tempV % eligibleMembers.size()).getName())

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
        for (Week week : plan.getWeeks()) {
            System.out.print("," + week.getStartDate()  + " : " + week.getEndDate() );
        }
        System.out.println();
        for (Person member : plan.getTeam().getTeamMember()) {
            String row = plan.getPersonWeeks().stream().filter(pw -> pw.person == member)
                    .sorted((o1, o2) -> (o1.week.getWeekNumber() - o2.week.getWeekNumber()))
                    .map(PersonWeek::getDescriptionWithLeaves)
                    .reduce(member.getName(), (a, b) -> a + "," + b);
            System.out.print(row);
            System.out.println();
        }
    }

    public String getPlanAsHtml() {

         DateTimeFormatter monthAndYear = new DateTimeFormatterBuilder()
                 .appendDayOfMonth(1)
                 .appendLiteral(' ')
                 .appendMonthOfYearShortText()
                   .toFormatter();


        String html = "<html>";
        html += "<style type=\"text/css\">td {border:1px solid grey;} th {border:1px solid grey;} table {border:1px solid grey;}</style>";
        html += "<body>";
        html += "<table>";
        html += "<tr>";
        html += "<th>Employee/Week</th>";
        for (Week week : plan.getWeeks()) {
            html += "<th>" + monthAndYear.print(  week.getStartDate())  + " : " + monthAndYear.print(week.getEndDate()) + "</th>";
        }
        html += "</tr>";
        for (Person member : plan.getTeam().getTeamMember()) {
            String row = plan.getPersonWeeks().stream().filter(pw -> pw.person == member)
                .sorted((o1, o2) -> (o1.week.getWeekNumber() - o2.week.getWeekNumber()))
                .map(PersonWeek::getPrettyHtmlDescription)
                .reduce("<td>" + member.getName() + "</td>", (a, b) -> a + "<td>" + b + "</td>");
            html += "<tr>" + row + "</tr>";
        }
        html += "</table>";
        html += "<body>";
        html += "</html>";
        return html;
    }

    public void populatePlan() {
        for(Person member : plan.getTeam().getTeamMember()) {
            for (Week week : plan.getWeeks()) {
                PersonWeek personWeek = new PersonWeek();
                personWeek.person = member;
                personWeek.week = week;
                plan.getPersonWeeks().add(personWeek);
            }
        }
    }

    public String addLeave(Person person, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        List<PersonWeek> weeksOfInterest = plan.getPersonWeeks().stream()
            .filter(personWeek -> StringUtils.equalsIgnoreCase(personWeek.person.getName(), person.getName()))
            .filter(personWeek -> isOverlappingWithLeaves(personWeek.week, leaveStartDate, leaveEndDate))
            .collect(Collectors.toList());
        for (PersonWeek personWeek : weeksOfInterest) {
            long noOfLeaveDays = 0;
            LocalDate leaveStartInWeek = leaveStartDate.isAfter(personWeek.week.getStartDate()) ? leaveStartDate : personWeek.week.getStartDate();
            LocalDate leaveEndInWeek = leaveEndDate.isBefore(personWeek.week.getEndDate()) ? leaveEndDate : personWeek.week.getEndDate();
            for (LocalDate date = leaveStartInWeek; !date.isAfter(leaveEndInWeek); date = date.plusDays(1)) {
                if (date.toDateTimeAtCurrentTime().getDayOfWeek() <= 5) {
                    noOfLeaveDays++;
                }
            }
            personWeek.leaves += noOfLeaveDays;
        }
        try {
            for (PersonWeek personWeek : weeksOfInterest) {
                if (personWeek.description.contains(ONCALL)) {

                    swapOncall(personWeek);
                }
            }
        } catch (Exception e) {
            return "Leaves Added. Unable to swap oncall. Please find a manual replacement...!!!";
        }
        return "Success";
    }

    private boolean isOverlappingWithLeaves(Week week, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        return (!week.getStartDate().isAfter(leaveStartDate) && !week.getEndDate().isBefore(leaveEndDate))
            || (!week.getStartDate().isBefore(leaveStartDate) && !week.getStartDate().isAfter(leaveEndDate))
            || (!week.getEndDate().isBefore(leaveStartDate) && !week.getEndDate().isAfter(leaveEndDate));
    }

    public void swapOncall(PersonWeek requesterCurrentWeek) {
        try {
            List<PersonWeek> requesteeCurrentWeeks = getPlanForWeek(requesterCurrentWeek.week.getStartDate())
                .stream()
                .filter(pw -> pw.leaves == 0)
                .filter(pw -> pw.person != requesterCurrentWeek.person)
                .filter(pw-> pw.person.getLevel().ordinal() > Level.PSE3.ordinal())
                .collect(Collectors.toList());

            List<PersonWeek> sortedRequesteeCurrentWeeks = requesteeCurrentWeeks.stream().sorted(new Comparator<PersonWeek>() {
                @Override
                public int compare(PersonWeek o1, PersonWeek o2) {
                    PersonWeek personWeek1 = getPlanForPerson(o1.person).stream()
                            .filter(pw -> pw.description.contains(ONCALL))
                            .filter(pw -> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                            .findFirst().get();

                    PersonWeek personWeek2 = getPlanForPerson(o2.person).stream()
                            .filter(pw -> pw.description.contains(ONCALL))
                            .filter(pw -> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                            .findFirst().get();

                    return personWeek1.week.getWeekNumber() - personWeek2.week.getWeekNumber();
                }
            }).collect(Collectors.toList());

            for (PersonWeek requesteeCurrentWeek : sortedRequesteeCurrentWeeks) {
                PersonWeek requesteeOncallWeek = getPlanForPerson(requesteeCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.description.contains(ONCALL))
                    .filter(pw-> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                    .findFirst().get();
                PersonWeek requesterOncallWeek = getPlanForPerson(requesterCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.week.getWeekNumber() == requesteeOncallWeek.week.getWeekNumber())
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

            if (StringUtils.equalsIgnoreCase(requesterCurrentWeek.description, ONCALL)) {
                throw new RuntimeException("Unable to swap oncall. Please figure a way out yourself...");
            }

        } catch (Exception e) {
            System.out.println("ERROR : Could not swap oncall");
        }
    }

    public List<PersonWeek> getPlanForWeek(LocalDate date) {
        return plan.getPersonWeeks().stream()
            .filter(pw -> !pw.week.getStartDate().isAfter(date))
            .filter(pw -> !pw.week.getEndDate().isBefore(date))
            .collect(Collectors.toList());
    }

    public PersonWeek getPlanForPersonWeek(Person person, LocalDate date) {
        return plan.getPersonWeeks().stream()
                .filter(pw -> StringUtils.equalsIgnoreCase(pw.person.getName(), person.getName()))
                .filter(pw -> !pw.week.getStartDate().isAfter(date))
                .filter(pw -> !pw.week.getEndDate().isBefore(date))
                .findFirst()
                .get();
    }

    public int getBandwidth() {
        return plan.getPersonWeeks().stream()
            .mapToInt(PersonWeek::unoccupied)
            .sum();
    }

    public List<PersonWeek> getPlanForPerson(Person person) {
        return plan.getPersonWeeks().stream()
            .filter(pw -> StringUtils.equalsIgnoreCase(pw.person.getName(), person.getName()))
            .collect(Collectors.toList());
    }

    public String getOncall(LocalDate date) {
        return plan.getPersonWeeks().stream()
            .filter(pw -> !pw.week.getStartDate().isAfter(date))
            .filter(pw -> !pw.week.getEndDate().isBefore(date))
            .filter(pw -> pw.description.contains("Oncall"))
            .findAny()
            .get()
            .person.getName();
    }
}
