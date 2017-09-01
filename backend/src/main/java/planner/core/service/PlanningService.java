package planner.core.service;

import org.joda.time.LocalDate;
import planner.core.model.Person;
import planner.core.model.PersonWeek;
import planner.core.model.Planner;
import planner.core.model.Team;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ankur.srivastava on 01/09/17.
 */
@Singleton
public class PlanningService {

    private Planner planner;
    private SetupService setupService;
    private Team team;

    @Inject
    public PlanningService(Planner planner, SetupService setupService) {
        this.planner = planner;
        this.setupService = setupService;
    }

    void checkAndSetTeam() {
        if (team == null) {
            team = setupService.getTeamByName("OFF").get(0);
            planner.setTeam(team);
        }

    }

    public String getPlanAsHtml() {
        checkAndSetTeam();
        return planner.getPlanAsHtml();
    }

    public void reset(String quarter) {
        checkAndSetTeam();
        planner.reset(quarter);
    }

    public void updateOKR(String okr) {
        checkAndSetTeam();
        planner.updateOKR(okr);
    }

    public PersonWeek getPlanForPersonWeek(String member, LocalDate date) {
        checkAndSetTeam();
        Person p = setupService.getPersonByName(member);
        return planner.getPlanForPersonWeek(p, date);
    }

    public void addLeave(String actor, LocalDate date, LocalDate date1) {
        checkAndSetTeam();
        Person p = setupService.getPersonByName(actor);
        planner.addLeave(p, date, date1);
    }

    public String getOncall(LocalDate date) {
        checkAndSetTeam();
        return planner.getOncall(date);
    }

    public int getBandwidth() {
        checkAndSetTeam();
        return  planner.getBandwidth();
    }
}
