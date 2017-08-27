package planner.core.service;

import planner.core.model.Team;
import planner.core.repository.PersonRepository;

import javax.inject.Inject;

/**
 * Created by ankur.srivastava on 26/08/17.
 */
public class SetupService {

    private PersonRepository personRepository;

    @Inject
    SetupService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Team createTeam() {
        return null;
    }

}
