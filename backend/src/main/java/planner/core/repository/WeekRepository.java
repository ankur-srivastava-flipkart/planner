package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Week;

import java.util.List;

/**
 * Created by ankur.srivastava on 02/09/17.
 */
public class WeekRepository extends AbstractDAO<Week> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    @Inject
    public WeekRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public void persist(List<Week> weeks) {
        weeks.stream().forEach(p -> super.persist(p));
    }

    public List<Week> fetchWeeksByQuarter(String quarter) {
        return list(namedQuery("planner.core.model.week.findByQuarter")
                .setParameter("quarter", "%" + quarter + "%"));
    }
}
