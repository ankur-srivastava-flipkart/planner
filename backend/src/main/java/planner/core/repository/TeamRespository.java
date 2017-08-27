package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Team;

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
}
