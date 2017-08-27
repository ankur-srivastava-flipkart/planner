package planner.core.repository;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import planner.core.model.Person;

import java.util.List;

/**
 * Created by ankur.srivastava on 26/08/17.
 */
public class PersonRepository extends AbstractDAO<Person>{

    @Inject
    public PersonRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Person> addPeople(List<Person> people) {
        people.stream().forEach(p -> persist(p));
        return people;
    }

    public List<Person> getAllPeople() {
        return list(namedQuery("planner.core.model.person.findAll"));
    }
}
