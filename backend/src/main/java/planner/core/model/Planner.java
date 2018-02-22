package planner.core.model;

import lombok.Data;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
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

    private Plan plan;

    public Planner withPlan(Plan plan) {
        this.plan = plan;
        return this;
    }

    public void updateOKR(Okr okr, List<Person> preferredResource, LocalDate preferredStartDate) {
        List<List<Person>> possibleAllocations = possibleAllocations(okr, preferredResource);
        blockPeople(okr,possibleAllocations,preferredStartDate == null ? new LocalDate() : preferredStartDate);
    }

    public List<List<Person>> possibleAllocations(Okr okr, List<Person> preferredResource) {
        List<Person> fixedResource = new ArrayList<>();
        Level maxLevel;

        if (!preferredResource.isEmpty()) {
            fixedResource.addAll(preferredResource);
            maxLevel = preferredResource.stream().max(new Comparator<Person>() {
                @Override
                public int compare(Person o1, Person o2) {
                    return o1.level.ordinal() - o2.level.ordinal();
                }
            }).get().getLevel();
        } else {
            maxLevel =  preferredMaxLevel(okr);
        }
        if (fixedResource.size() >= okr.getParallelism()) {
            return Permute.getAllCombinations(fixedResource.toArray(new Person[fixedResource.size()]),fixedResource.size(), okr.getParallelism());
        }

        Level finalMaxLevel = maxLevel;
        List<Person> availableResources = plan.getTeam().getTeamMember().stream()
                .filter(e -> e.level.ordinal() < finalMaxLevel.ordinal())
                .filter(e -> !fixedResource.contains(e))
                .collect(Collectors.toList());

        int permuteBatchSize = okr.getParallelism() - fixedResource.size();
        if (permuteBatchSize > availableResources.size()) {
            permuteBatchSize = availableResources.size();
        }

        List<List<Person>> variableResources = Permute.getAllCombinations(availableResources.toArray(new Person[availableResources.size()]),
                                                    availableResources.size(),
                                                    permuteBatchSize);
        variableResources.stream().forEach( p-> p.addAll(fixedResource));


        return variableResources;
    }

    public Level preferredMaxLevel(Okr eachOkr) {
        Level maxLevel = Level.SDE3;
            if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays <=3) {
                maxLevel = Level.PSE3;
            } else if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays <=10) {
                maxLevel = Level.SDE1;
            } else if (eachOkr.complexity == Complexity.SIMPLE && eachOkr.effortinPersonDays >10) {
                maxLevel = Level.SDE1;
            } else if (eachOkr.complexity == Complexity.MEDIUM && eachOkr.effortinPersonDays <=10) {
                maxLevel = Level.SDE2;
            } else if (eachOkr.complexity == Complexity.MEDIUM && eachOkr.effortinPersonDays >10) {
                maxLevel = Level.SDE2;
            } else if (eachOkr.complexity == Complexity.COMPLEX && eachOkr.effortinPersonDays <=10) {
                maxLevel = Level.SDE2G10;
            }  else if (eachOkr.complexity == Complexity.COMPLEX && eachOkr.effortinPersonDays > 10) {
                maxLevel = Level.SDE2G10;
            }
        return maxLevel;
    }

    public void blockPeople( Okr okr, List<List<Person>> allCombinations, LocalDate preferredStartDate) {
        Map<List<Person>, LocalDate> endDateMap = new HashMap<>();
        List<Week> preferredWeeks = plan.getWeeks().stream().filter(p -> p.getStartDate().isAfter(preferredStartDate)).collect(Collectors.toList());
        for(List<Person> eachCombination : allCombinations) {
            int effortRemaining = okr.effortinPersonDays;
            Week lastWeek = null;
            for (Week week : preferredWeeks) {
                double totalWeekEffort = 0;

                for (Person member : eachCombination) {
                    PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.getStartDate());
                    totalWeekEffort += (5 - planForPersonWeek.occupied() - planForPersonWeek.leaves)*member.productivity;
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

        for (Week week : preferredWeeks) {
            for (Person member :  listLocalDateEntry.getKey()) {
                PersonWeek planForPersonWeek = getPlanForPersonWeek(member, week.getStartDate());
                if(planForPersonWeek.unoccupied() == 0 ) {
                    continue;
                }
                if (efforRemaining >= planForPersonWeek.unoccupied() * member.productivity) {
                    float effortPlannedForCurrentWeek = planForPersonWeek.unoccupied() * member.productivity;
                    efforRemaining -= effortPlannedForCurrentWeek;
                    planForPersonWeek.okrAllocations.add(new OkrAllocation(okr, planForPersonWeek.unoccupied()));
                } else {
                    efforRemaining -= efforRemaining;
                    planForPersonWeek.okrAllocations.add(new OkrAllocation(okr , (float)Math.ceil(efforRemaining / member.productivity)));
                }
                if ((int)efforRemaining == 0) {
                    break;
                }
            }
            if ((int)efforRemaining == 0) {
                break;
            }
        }
        okr.setSpillOver((int)efforRemaining);
    }

    public void populateDevOps(Okr devOpsOkr) {

        List<Person> eligibleMembers = plan.getTeam().getTeamMember().stream().filter(p -> p.getLevel().ordinal() <= Level.PSE3.ordinal())
                .collect(Collectors.toList());

        plan.getPersonWeeks().stream()
                .filter(pw -> eligibleMembers.contains(pw.getPerson()))
                .forEach(pw ->
                            pw.okrAllocations.add(new OkrAllocation(devOpsOkr,(float)(5-pw.getLeaves()) * 0.33f))
                );
    }

    public void populateOncall(Okr oncall) {
        int i = 0;
        CircularFifoBuffer last4Oncalls = new CircularFifoBuffer(3);
        int first4 = 32;

        for (Week week : plan.getWeeks()) {
            final int tempV = i;
            final int temp = first4;
            System.out.println(tempV % plan.getTeam().getTeamMember().size() + 1);

            List<Person> eligibleMembers = plan.getTeam().getTeamMember().stream().filter(p -> p.getLevel().ordinal() > Level.PSE3.ordinal()
                                                            && p.getLevel().ordinal() < Level.ARCH.ordinal())
                    .sorted(new Comparator<Person>() {
                        @Override
                        public int compare(Person o1, Person o2) {
                            return o1.getId().compareTo(o2.getId());
                        }
                    })
                    .collect(Collectors.toList());

            List<PersonWeek> matchedWeeks = plan.getPersonWeeks().stream().filter(pw ->
                    pw.week.getWeekNumber() == week.getWeekNumber())
                    .filter(pw -> pw.getOccupied() < 1)
                    .filter(pw -> pw.getLeaves() <=1)
                    .filter(pw -> eligibleMembers.contains(pw.getPerson()))
                            .filter(pw -> !last4Oncalls.contains(pw.getPerson()) || temp != 0)

            .collect(Collectors.toList());

            if (matchedWeeks.isEmpty()) {
                throw new RuntimeException("None of the weeks matched");
            }

            PersonWeek selectedWeek = matchedWeeks.get(tempV % matchedWeeks.size());
            selectedWeek.okrAllocations.add(new OkrAllocation(oncall, 5 - matchedWeeks.get(0).leaves ));
            last4Oncalls.add(selectedWeek.getPerson());
            first4 = first4 >> 1;
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

    public void addPlanForPerson(Person member, String effectiveFrom) {
            for (Week week : plan.getWeeks()) {
                PersonWeek personWeek = new PersonWeek();
                personWeek.person = member;
                personWeek.week = week;
                if (week.getStartDate().isBefore(LocalDate.parse(effectiveFrom))) {
                    personWeek.setLeaves(5);
                }
                plan.getPersonWeeks().add(personWeek);
            }
    }


    public String removeLeave(Person person, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        List<PersonWeek> weeksOfInterest = plan.getPersonWeeks().stream()
                .filter(personWeek -> StringUtils.equalsIgnoreCase(personWeek.person.getName(), person.getName()))
                .filter(personWeek -> isOverlappingWithLeaves(personWeek.week, leaveStartDate, leaveEndDate))
                .collect(Collectors.toList());
        for (PersonWeek personWeek : weeksOfInterest) {
            int noOfLeaveDays = personWeek.getLeaves();
            LocalDate leaveStartInWeek = leaveStartDate.isAfter(personWeek.week.getStartDate()) ? leaveStartDate : personWeek.week.getStartDate();
            LocalDate leaveEndInWeek = leaveEndDate.isBefore(personWeek.week.getEndDate()) ? leaveEndDate : personWeek.week.getEndDate();
            for (LocalDate date = leaveStartInWeek; !date.isAfter(leaveEndInWeek); date = date.plusDays(1)) {
                if (date.toDateTimeAtCurrentTime().getDayOfWeek() <= 5) {
                    noOfLeaveDays = noOfLeaveDays > 0 ? --noOfLeaveDays : noOfLeaveDays;
                }
            }
            personWeek.leaves = noOfLeaveDays;
        }
        return "done";
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
                weeksOfInterest.stream()
                        .filter(pw -> pw.isOncall())
                        .forEach( pw ->
                            swapOncall(pw, pw.getOncallOkr()));
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

    public void swapOncall(PersonWeek requesterCurrentWeek, Okr oncall) {
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
                            .filter(pw -> pw.isOncall())
                            .filter(pw -> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                            .findFirst().get();

                    PersonWeek personWeek2 = getPlanForPerson(o2.person).stream()
                            .filter(pw -> pw.isOncall())
                            .filter(pw -> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                            .findFirst().get();

                    return personWeek1.week.getWeekNumber() - personWeek2.week.getWeekNumber();
                }
            }).collect(Collectors.toList());

            for (PersonWeek requesteeCurrentWeek : sortedRequesteeCurrentWeeks) {
                PersonWeek requesteeOncallWeek = getPlanForPerson(requesteeCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.isOncall())
                    .filter(pw-> pw.week.getWeekNumber() > requesterCurrentWeek.week.getWeekNumber())
                    .findFirst().get();
                PersonWeek requesterOncallWeek = getPlanForPerson(requesterCurrentWeek.person)
                    .stream()
                    .filter(pw -> pw.week.getWeekNumber() == requesteeOncallWeek.week.getWeekNumber())
                    .findAny()
                    .get();
                if (requesterOncallWeek.leaves == 0) {
                    requesterOncallWeek.getOkrAllocations().add(new OkrAllocation(oncall, 5));
                    requesteeOncallWeek.getOkrAllocations().removeIf(p -> StringUtils.equalsIgnoreCase(p.getOkr().description, "ONCALL"));
                    requesterCurrentWeek.getOkrAllocations().removeIf(p -> StringUtils.equalsIgnoreCase(p.getOkr().description, "ONCALL"));
                    requesteeCurrentWeek.getOkrAllocations().add(new OkrAllocation(oncall, 5));
                    break;
                }
            }

            if (requesterCurrentWeek.isOncall()) {
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

    public double getAvailableBandwidthForQuarter() {
        return plan.getPersonWeeks().stream()
            .mapToDouble(PersonWeek::getAvailableBandWidth)
            .sum();
    }

    public double getRemainingBandwidthForQuarter(LocalDate startDate) {
        return plan.getPersonWeeks().stream()
                .filter(p -> p.week.getStartDate().isAfter(startDate))
                .mapToDouble(PersonWeek::getAvailableBandWidth)
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


    public void removeOkr(Integer okrId) {
        plan.getPersonWeeks().forEach(
                pw-> pw.okrAllocations.removeIf(al -> al.getOkr().getId() == okrId)
        );
    }


}
