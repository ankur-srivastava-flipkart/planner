package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Plan;
import planner.core.model.Team;

/**
 * Created by ankur.srivastava on 03/09/17.
 */
public class PlanRepository extends AbstractDAO<Plan> {

    @Inject
    public PlanRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Plan loadPlan(String quarter, Team team1) {
        return uniqueResult(namedQuery("planner.core.model.plan.findByTeamQuarter")
                .setParameter("quarter",quarter)
                .setParameter("team", "%" + team1.getName() + "%")
        );
    }

    public Plan savePlan(Plan plan) {
        return persist(plan);
    }

    public void deletePlan(Plan plan) {
        super.currentSession().delete(plan);
    }
}
