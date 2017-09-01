package planner.core.service;

import org.apache.commons.lang3.StringUtils;
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
    public SetupService(PersonRepository personRepository, TeamRespository teamRespository) {
        this.personRepository = personRepository;
        this.teamRespository = teamRespository;
    }

    public Team createTeam(CreateTeamRequest createTeamRequest) {
        Person em = personRepository.getPersonByName(createTeamRequest.getEm());
        List<Person> teamMembers = createTeamRequest.getTeamMembers().stream()
                .map(tm -> personRepository.getPersonByName(tm))
                .collect(Collectors.toList());

        Team team = new Team(createTeamRequest.getTeamName(), em, teamMembers);
        teamRespository.createTeam(team);

        return team;
    }

    public List<Person> createPeople (List<CreatePersonRequest> people) {
        return personRepository.addPeople(convertPersonEntity(people));
    }

    private List<Person> convertPersonEntity(List<CreatePersonRequest> people) {
        List<Person> collect = people.stream()
                .map(p -> new Person(p.getName(), p.getEmail(), p.getProductivity(), p.getLevel()))
                .collect(Collectors.toList());
        return collect;
    }

    public List<Person> getAllPeople() {
        return personRepository.getAllPeople();
    }

    public Person getPersonByName(String name) {
        return personRepository.getPersonByName(name);
    }


    public List<Team> getAllTeams() {
        return teamRespository.getAllTeams();
    }

    public List<Team> getTeamByName(String name) {
        return teamRespository.getTeamsByName(name);
    }

    public Team removeTeamMember(String teamName, String personName) {
        Team team = teamRespository.getTeamByName(teamName);
        team.getTeamMember().removeIf(member -> StringUtils.equalsIgnoreCase(member.getName(), personName));

        return team;
    }

    public Team addTeamMember(String teamName, String personName) {
        Team team = teamRespository.getTeamByName(teamName);
        team.getTeamMember().add(personRepository.getPersonByName(personName));
        return team;
    }

    public Person updatePerson(String personName, CreatePersonRequest personRequest) {
        Person person = personRepository.getPersonByName(personName);
        if (!StringUtils.isEmpty(personRequest.getName())) { person.setName(personRequest.getName());}
        if (!StringUtils.isEmpty(personRequest.getEmail())) { person.setEmail(personRequest.getEmail());}
        if (personRequest.getLevel() != null) { person.setLevel(personRequest.getLevel());}
        if (personRequest.getProductivity() != null) { person.setProductivity(personRequest.getProductivity());}
        return person;
    }
}
