package planner.core.service;

import planner.core.dto.CreatePersonRequest;
import planner.core.dto.CreateTeamRequest;
import planner.core.model.Person;
import planner.core.model.Team;
import planner.core.repository.PersonRepository;
import planner.core.repository.TeamRespository;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ankur.srivastava on 26/08/17.
 */
public class SetupService {

    private PersonRepository personRepository;
    private TeamRespository teamRespository;

    @Inject
    SetupService(PersonRepository personRepository, TeamRespository teamRespository) {
        this.personRepository = personRepository;
        this.teamRespository = teamRespository;
    }

    public Team createTeam(CreateTeamRequest createTeamRequest) {
        return null;
    }

    public List<Person> createPeople (List<CreatePersonRequest> people) {
        return personRepository.addPeople(convertPersonEntity(people));
    }

    private List<Person> convertPersonEntity(List<CreatePersonRequest> people) {
        List<Person> collect = people.stream()
                .map(p -> new Person(p.getName(), p.getEmail()))
                .collect(Collectors.toList());
        return collect;
    }

    public List<Person> getAllPeople() {
        return personRepository.getAllPeople();

    }
}
