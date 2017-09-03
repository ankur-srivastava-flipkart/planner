package planner.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankur.srivastava on 26/08/17.
 */

@Entity
@Data
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "planner.core.model.team.findAll",
                query = "select p from Team p"),
        @NamedQuery(name = "planner.core.model.team.findByName",
                query = "select p from Team p"
                        + " where p.name LIKE :name")
})
public class Team {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToOne
    private Person em;

    @OneToMany
    List<Person> teamMember = new ArrayList<>();

    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    List<Okr> okr;

    public Team(String teamName, Person em, List<Person> teamMembers) {
        this.name=teamName;
        this.em = em;
        this.teamMember=teamMembers;
    }

    public void addOkr(Okr p) {
        okr.add(p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
       // if (!super.equals(o)) return false;

        Team team = (Team) o;

        return name.equals(team.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
