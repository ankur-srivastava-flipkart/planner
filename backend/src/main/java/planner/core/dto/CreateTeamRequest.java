package planner.core.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by ankur.srivastava on 27/08/17.
 */
@Data
public class CreateTeamRequest {
    String teamName;
    String em;
    List<String> teamMembers;
}
