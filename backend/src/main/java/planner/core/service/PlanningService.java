package planner.core.service;

import org.joda.time.LocalDate;
import planner.core.model.*;
import planner.core.repository.OkrRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 01/09/17.
 */
public class PlanningService {

    private Planner planner;
    private SetupService setupService;
    private Team team;
    private OkrRepository okrRepository;

    @Inject
    public PlanningService(Planner planner, SetupService setupService, OkrRepository okrRepository) {
        this.planner = planner;
        this.setupService = setupService;
        this.okrRepository = okrRepository;
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

    public List<Okr> updateOKR(String okr) {
        checkAndSetTeam();

        List<Okr> okrList = Arrays.stream(okr.split("\\*"))
                .map(Okr::new)
               // .map(p-> {team.addOkr(p);  p.setTeam(team); return p;})
                .collect(Collectors.toList());

        okrList.stream().forEach(p-> {p.setTeam(team);});
        List<Okr> alreadyPresentOks = okrList.stream().filter(team.getOkr()::contains).collect(Collectors.toList());
        List<Okr> newOkr = okrList.stream().filter(p -> !alreadyPresentOks.contains(p)).collect(Collectors.toList());

        planner.updateOKR(newOkr);
        newOkr.stream().forEach(p-> {team.addOkr(p);});
        okrRepository.persist(newOkr);
        setupService.saveTeam(team);
        return alreadyPresentOks;
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
