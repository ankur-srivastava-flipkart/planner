package planner.core.dto;

import planner.core.model.Person;
import planner.core.model.TeamMember;

import java.util.List;

/**
 * Created by ankur.srivastava on 27/08/17.
 */
public class CreateTeamRequest {
    String teamName;
    String em;
    List<String> teamMembers;
}
