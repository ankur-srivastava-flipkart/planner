package planner.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * Created by ankur.srivastava on 26/08/17.
 */

@Entity
@Data
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToOne
    private Person em;

    @OneToMany
    List<Person> teamMember;

    public Team(String teamName, Person em, List<Person> teamMembers) {
        this.name=teamName;
        this.em = em;
        this.teamMember=teamMembers;
    }
}
