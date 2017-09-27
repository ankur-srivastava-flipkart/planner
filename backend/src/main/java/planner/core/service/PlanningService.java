package planner.core.service;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import planner.core.model.*;
import planner.core.repository.OkrRepository;
import planner.core.repository.PlanRepository;
import planner.core.repository.WeekRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<Week> fetchOrCreateWeeks(String quarter) {
        List<Week> weeks = weekRepository.fetchWeeksByQuarter(quarter);
        if (!weeks.isEmpty()) {
            return weeks;
        }

        int startingMonth = getStartingMonth(quarter);
        LocalDate startDate = new LocalDate().withYear(2017).withMonthOfYear(startingMonth + 1).dayOfMonth().withMinimumValue();
        LocalDate endDate = new LocalDate().withYear(2017).withMonthOfYear(startingMonth + quarter.length()).dayOfMonth().withMaximumValue();

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

        weekRepository.persist(weeks);
        return weeks;
    }

    public Plan createNewPlan(Team team, String quarter) {
        List<Week> weeks = fetchOrCreateWeeks(quarter);
        Plan plan = new Plan();
        Planner planner = new Planner();
        plan.setTeam(team);
        plan.setQuarter(quarter);
        plan.setWeeks(weeks);
        planner.withPlan(plan).populatePlan();
        planner.withPlan(plan).printPlan();
        return plan;
    }

    public Plan reset(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        try {
            Plan plan = fetchPlan(quarter, team1);
            planRepository.deletePlan(plan);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Plan plan = createNewPlan(team1, quarter);
        planRepository.savePlan(plan);
        return plan;
    }

    public List<Okr> updateOKR(String team, String quarter, String okr) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);

        List<Okr> okrList = Arrays.stream(okr.split("\\*"))
                .map(Okr::new)
                .map(p -> {p.setQuarter(quarter); return p; } )
                .collect(Collectors.toList());

        okrList.stream().forEach(p -> {
            p.setTeam(team1);
        });
        List<Okr> alreadyPresentOks = okrList.stream().filter(team1.getOkr()::contains).collect(Collectors.toList());
        List<Okr> newOkr = okrList.stream().filter(p -> !alreadyPresentOks.contains(p)).collect(Collectors.toList());

      //  planner.updateOKR(newOkr);
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
        planner.populateDevOps(okrRepository.getOkrByDescription("DEVOPS", team1.getId()));
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
        planner.populateOncall(okrRepository.getOkrByDescription("ONCALL", team1.getId()));
        planRepository.savePlan(planner.getPlan());
    }

    public Set<Okr> getAllOKR(String team, String quarter) {
        Team team1 = validateTeamAndQuarter(team, quarter);
        Planner planner = fetchPlanner(quarter, team1);
        Set<Okr> collect = planner.getPlan().getPersonWeeks().stream()
                .flatMap(pw -> pw.getOkrList().stream())
                .collect(Collectors.toSet());
        return collect;

    }
}
