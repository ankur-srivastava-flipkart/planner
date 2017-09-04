package planner.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "planner.core.model.plan.findByTeamQuarter",
                query = "select p from Plan p where p.quarter like :quarter and p.team.name like :team")
})
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<PersonWeek> personWeeks = new ArrayList<PersonWeek>();

    @OneToMany
    List<Week> weeks = new ArrayList<Week>();

    @OneToOne
    Team team;

    String quarter;
}