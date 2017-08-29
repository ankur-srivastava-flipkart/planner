package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Team;

import java.util.List;

/**
 * Created by ankur.srivastava on 27/08/17.
 */
public class TeamRespository extends AbstractDAO<Team> {

    @Inject
    public TeamRespository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Team createTeam(Team team) {
        return persist(team);
    }

    public List<Team> getAllTeams() {
        return list(namedQuery("planner.core.model.team.findAll"));
    }

    public Team getTeamByName(String name) {
        return uniqueResult(namedQuery("planner.core.model.team.findByName")
                .setParameter("name","%" + name + "%"));
    }

    public List<Team> getTeamsByName(String name) {
        return list(namedQuery("planner.core.model.team.findByName")
                .setParameter("name","%" + name + "%"));
    }
}
