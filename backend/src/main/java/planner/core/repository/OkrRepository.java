package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Okr;

import java.util.List;

/**
 * Created by ankur.srivastava on 02/09/17.
 */
public class OkrRepository extends AbstractDAO<Okr> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    @Inject
    public OkrRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Okr> findAllOkrByTeamAndQuarter(String team, String quarter) {
        return list(namedQuery("planner.core.model.okr.findAllByTamQuarter")
                .setParameter("name","%" + team + "%")
                .setParameter("quarter", "%" + quarter + "%"));
    }

    public void persist(List<Okr> newOkr) {
        newOkr.stream().forEach(p -> super.persist(p));
    }

    public Okr getOkrByDescription(String name, Integer id) {
        return uniqueResult(namedQuery("planner.core.model.okr.findByName")
                .setParameter("name","%" + name + "%")
                .setParameter("team", id));
    }

}
