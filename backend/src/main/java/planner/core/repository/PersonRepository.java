package planner.core.repository;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Person;

/**
 * Created by ankur.srivastava on 26/08/17.
 */
public class PersonRepository extends AbstractDAO<Person>{

    public PersonRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
