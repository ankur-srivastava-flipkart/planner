package planner.core.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import planner.core.dto.AddOkrRequest;
import planner.core.dto.OkrExecutionView;
import planner.core.model.*;
import planner.core.repository.OkrRepository;
import planner.core.repository.PlanRepository;
import planner.core.repository.WeekRepository;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ankur.srivastava on 01/09/17.
 */
public class PlanningService {

    private SetupService setupService;
    private OkrRepository okrRepository;
    private PlanRepository planRepository;
    private WeekRepository weekRepository;

    @Inject
    public PlanningService(SetupService setupService, OkrRepository okrRepository, PlanRepository planRepository, WeekRepository weekRepository) {
        this.setupService = setupService;
        this.okrRepository = okrRepository;
        this.planRepository = planRepository;
        this.weekRepository = weekRepository;
    }

    public Team validateTeamAndQuarter(String team, String quarter) {
        List<Team> team1 = setupService.getTeamByName(team);
        if (team1.isEmpty()) {
            throw new RuntimeException("No Team with Given Name found.");
        }
        if (team1.size() > 1) {
            throw new RuntimeException("Ambiguous Team name.");
        }

        getStartingMonth(quarter);
        return team1.get(0);
    }

    public String getPlanAsHtml(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return planner.getPlanAsHtml();
    }

    public Planner getPlan(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return planner;
    }

    private Plan fetchPlan(String quarter, Team team1) {
        Plan plan = planRepository.loadPlan(quarter, team1);
        if (plan == null) {
            throw new RuntimeException("No current personWeeks exists for this team/quarter. Please use the reset API to create a personWeeks.");
        }
        return plan;
    }

    public void deletePlan(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Plan plan = planRepository.loadPlan(quarter, team1);
        if (plan == null) {
            throw new RuntimeException("No current personWeeks exists for this team/quarter. Please use the reset API to create a personWeeks.");
        }
        planRepository.deletePlan(plan);
    }

    private Planner fetchPlanner(String quarter, Team team1) {
        Plan plan = planRepository.loadPlan(quarter, team1);
        if (plan == null) {
            throw new RuntimeException("No current personWeeks exists for this team/quarter. Please use the reset API to create a personWeeks.");
        }
        return new Planner().withPlan(plan);
    }

    public static int getStartingMonth(String quarter) {
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

    @Deprecated
    public void extendMayInJFMA(String team) {
        List<Week> weeksExpected = createWeeks("JFMAM", 2018, 2018);

        List<Week> weeksOriginal = weekRepository.fetchWeeksByQuarter("JFMA");

        List<Week> remainderWeeks = weeksExpected.stream().filter( w -> w.getWeekNumber() > 18).collect(Collectors.toList());

        remainderWeeks.stream().forEach(w -> w.setQuarter("JFMA"));
        // add extra weeks
        weekRepository.persist(remainderWeeks);

        Team team1 = validateTeamAndQuarter(team, "JFMA");
        Planner planner = fetchPlanner("JFMA", team1);

        // add weeks to existing plan
        Plan plan = planner.getPlan();
        plan.getWeeks().addAll(remainderWeeks);

        // add person weeks to plan
        for(Person member : plan.getTeam().getTeamMember()) {
            for (Week week : remainderWeeks) {
                PersonWeek personWeek = new PersonWeek();
                personWeek.setPerson(member);
                personWeek.setWeek(week);
                plan.getPersonWeeks().add(personWeek);
            }
        }

        // add oncall

        Okr oncall = okrRepository.getOkrByDescription("ONCALL", team1.getId(), "JFMA");

        int i = 0;
        for (Week week : remainderWeeks) {
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
                    pw.getWeek().getWeekNumber() == week.getWeekNumber() &&
                            StringUtils.equals(pw.getPerson().getName(), eligibleMembers.get(tempV % eligibleMembers.size()).getName())

            ).collect(Collectors.toList());
            if (matchedWeeks.size() > 1) {
                System.out.println(matchedWeeks);
                throw new RuntimeException("More than one weeks matched");
            }
            matchedWeeks.get(0).getOkrAllocations().add(new OkrAllocation(oncall, 5 - matchedWeeks.get(0).getLeaves() ));
            i = i+1;
        }

        planRepository.savePlan(plan);
        return ;

    }

    public List<Week> createWeeks(String quarter,  int startYear, int endYear) {
        List<Week> weeks = Lists.newArrayList();
        int startingMonth = getStartingMonth(quarter);
        LocalDate startDate = new LocalDate().withYear(startYear).withMonthOfYear(startingMonth + 1).dayOfMonth().withMinimumValue();
        LocalDate endDate = new LocalDate().withYear(endYear).withMonthOfYear(startingMonth + quarter.length()).dayOfMonth().withMaximumValue();

        int weekNumber = 1;
        while (startDate.isBefore(endDate.plusDays(7))) {
            Week e = new Week();
            e.setWeekNumber(weekNumber);
            e.setStartDate(startDate.withDayOfWeek(DateTimeConstants.MONDAY));
            e.setEndDate(startDate.withDayOfWeek(DateTimeConstants.SUNDAY));
            e.setQuarter(quarter);
            weeks.add(e);
            System.out.println(startDate.withDayOfWeek(DateTimeConstants.MONDAY) + " - " + startDate.withDayOfWeek(DateTimeConstants.SUNDAY));
            startDate = startDate.plusDays(7);
            weekNumber += 1;
        }
        return weeks;
    }

    public List<Week> fetchOrCreateWeeks(String quarter,  int startYear, int endYear) {
        List<Week> weeks = weekRepository.fetchWeeksByQuarter(quarter);
        if (!weeks.isEmpty()) {
            return weeks;
        }
        weeks = createWeeks(quarter, startYear, endYear);
        weekRepository.persist(weeks);
        return weeks;

    }

    public Plan createNewPlan(Team team, String quarter, int startYear, int endYear) {
        List<Week> weeks = fetchOrCreateWeeks(quarter, startYear, endYear);
        Plan plan = new Plan();
        Planner planner = new Planner();
        plan.setTeam(team);
        plan.setQuarter(quarter);
        plan.setWeeks(weeks);
        planner.withPlan(plan).populatePlan();
        planner.withPlan(plan).printPlan();
        return plan;
    }

    public Plan reset(String team, String quarter,  int startYear, int endYear) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        try {
            Plan plan = fetchPlan(quarter, team1);
            planRepository.deletePlan(plan);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Plan plan = createNewPlan(team1, quarter, startYear, endYear);
        planRepository.savePlan(plan);
        return plan;
    }

    public void rePlan(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);

        getAllOKR(team, quarter).stream().filter(p -> !p.getDescription().equalsIgnoreCase("ONCALL") && !p.getDescription().equalsIgnoreCase("DEVOPS"))
                .forEach(p -> removeOkr(team, quarter, p.getId()));

        okrRepository.findAllOkrByTeamAndQuarter(team, quarter).stream()
                .filter(p -> !p.getDescription().equalsIgnoreCase("ONCALL") && !p.getDescription().equalsIgnoreCase("DEVOPS"))
                .sorted(new Comparator<Okr>() {
                    @Override
                    public int compare(Okr o1, Okr o2) {
                        return o1.getPriority() - o2.getPriority();
                    }
                }).forEach(okr -> {
            List<Person> preferredResources = new ArrayList<>();
            if (StringUtils.isNotBlank(okr.getPreferredResource())) {
                preferredResources = Arrays.stream(okr.getPreferredResource().split(","))
                        .map(e -> setupService.getPersonByName(e))
                        .collect(Collectors.toList());
            }
            planner.updateOKR(okr, preferredResources, okr.getPreferredStartDate());
        });
    }

    public void planOkr(String team, String quarter, List<AddOkrRequest> okrs) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);

        Stream<AddOkrRequest> sortedOkrs = okrs.stream().sorted(new Comparator<AddOkrRequest>() {
            @Override
            public int compare(AddOkrRequest o1, AddOkrRequest o2) {
                Okr okr1 = okrRepository.getOkrByDescription(o1.getOkr(), team1.getId(), quarter);
                Okr okr2 = okrRepository.getOkrByDescription(o1.getOkr(), team1.getId(), quarter);
                return okr1.getPriority() - okr2.getPriority();
            }
        });
        sortedOkrs.forEach(
                p ->  {
                    Okr okr = okrRepository.getOkrByDescription(p.getOkr(), team1.getId(), quarter);
                    List<Person> preferredResource = p.getPreferredResource().stream().map(e -> setupService.getPersonByName(e)).collect(Collectors.toList());
                    planner.updateOKR(okr, preferredResource, p.getPreferredStartDate());
                    okr.setPreferredResource(p.getPreferredResource().stream().collect(Collectors.joining(",")));
                    okr.setPreferredStartDate(p.getPreferredStartDate());
                }
        );
    }



    public List<Okr> addOkr(String team, String quarter, String okr) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        List<Okr> okrList = Arrays.stream(okr.split("\\*"))
                .map(Okr::new)
                .map(p -> {p.setQuarter(quarter); return p; } )
                .collect(Collectors.toList());

        okrList.stream().forEach(p -> {
            p.setTeam(team1);
        });
        List<Okr> alreadyPresentOks = okrList.stream().filter(team1.getOkr()::contains).collect(Collectors.toList());
        List<Okr> newOkr = okrList.stream().filter(p -> !alreadyPresentOks.contains(p)).collect(Collectors.toList());

        newOkr.stream().forEach(p -> {
            team1.addOkr(p);
        });
        okrRepository.persist(newOkr);
        setupService.saveTeam(team1);
        return alreadyPresentOks;
    }

    public PersonWeek getPlanForPersonWeek(String team, String quarter, String member, LocalDate date) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        Person p = setupService.getPersonByName(member);
        return planner.getPlanForPersonWeek(p, date);
    }

    public String addLeave(String team, String quarter, String actor, LocalDate date, LocalDate date1) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        Person p = setupService.getPersonByName(actor);
        return planner.addLeave(p, date, date1);
    }

    public String removeLeave(String team, String quarter, String actor, LocalDate date, LocalDate date1) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        Person p = setupService.getPersonByName(actor);
        return planner.removeLeave(p, date, date1);
    }

    public String getOncall(String team, String quarter, LocalDate date) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return planner.getOncall(date);
    }

    public int getBandwidth(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return new Double(planner.getAvailableBandwidthForQuarter()).intValue();
    }

    public int getRemainingBandwidth(String team, String quarter, LocalDate startDate) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return new Double(planner.getRemainingBandwidthForQuarter(startDate)).intValue();
    }

    public void resetPlanForMember(String team, String quarter, String actor) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        Person p = setupService.getPersonByName(actor);
        planner.getPlan().getPersonWeeks().removeIf(pw -> pw.getPerson().getId() == p.getId());
        planner.addPlanForPerson(p);
        planRepository.savePlan(planner.getPlan());
    }

    public void addDevOps(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        planner.populateDevOps(okrRepository.getOkrByDescription("DEVOPS", team1.getId(), quarter));
        planRepository.savePlan(planner.getPlan());
    }

    public void removeOkr(String team, String quarter, Integer okrId) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        planner.removeOkr(okrId);
        planRepository.savePlan(planner.getPlan());
    }

    public void populateOncall(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        planner.populateOncall(okrRepository.getOkrByDescription("ONCALL", team1.getId(), quarter));
        planRepository.savePlan(planner.getPlan());
    }

    public Set<Okr> getAllOKR(String team, String quarter) {
        Set<Okr> collect = okrRepository.findAllOkrByTeamAndQuarter(team, quarter).stream()
                .collect(Collectors.toSet());
        return collect;
    }

    private OkrExecutionView createOkrExecutionView(Okr okr, Planner planner) {
        Comparator<PersonWeek> personWeekOkrComparator = new Comparator<PersonWeek>() {
            @Override
            public int compare(PersonWeek o1, PersonWeek o2) {
                return o1.getWeek().getWeekNumber() - o2.getWeek().getWeekNumber();
            }
        };

        Optional<PersonWeek> minWeek = planner.getPlan().getPersonWeeks().stream()
                .filter(p -> p.getOkrAllocations().stream().map(OkrAllocation::getOkr).anyMatch(q -> q.equals(okr))).min(personWeekOkrComparator);

        Optional<PersonWeek> maxWeek = planner.getPlan().getPersonWeeks().stream()
                .filter(p -> p.getOkrAllocations().stream().map(OkrAllocation::getOkr).anyMatch(q -> q.equals(okr))).max(personWeekOkrComparator);

        if (minWeek.isPresent() && maxWeek.isPresent())
            return  new OkrExecutionView(okr, minWeek.get().getWeek().getStartDate(),  maxWeek.get().getWeek().getEndDate());
        else
            return new OkrExecutionView(okr, null, null);
    }

    public List<OkrExecutionView> getAllPlannedOKR(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        return getAllOKR(team,quarter).stream().map(p-> createOkrExecutionView(p, planner)).collect(Collectors.toList());
    }
}
