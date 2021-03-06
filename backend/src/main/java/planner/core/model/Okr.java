package planner.core.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.LocalDate;

import javax.persistence.*;

/**
 * Created by ankur.srivastava on 23/06/17.
 */

@Entity
@Data
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "planner.core.model.okr.findAllByTamQuarter",
                query = "select p from Okr p where p.quarter like :quarter and p.team.name like :name"),
        @NamedQuery(name = "planner.core.model.okr.findByName",
                query = "select p from Okr p"
                        + " where p.description LIKE :name and p.team.id = :team and p.quarter = :quarter")

})
public class Okr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String quarter;
    String description;
    String jiraEpic;
    int effortinPersonDays;
    Complexity complexity;
    int priority;
    int parallelism;
    int status = 0;
    int spillOver = 0;
    String preferredResource = "";
    LocalDate preferredStartDate;

    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
    @ManyToOne
    Team team;

    @Override
    public String toString() {
        return this.description;
    }

    public Okr(String e) {
        try {
            String[] split = e.split(":");
            this.description = split[0];
            this.jiraEpic = split[1];
            this.effortinPersonDays = Integer.parseInt(split[2]);
            this.complexity = Complexity.valueOf(split[3]);
            this.priority = Integer.parseInt(split[4]);
            this.parallelism = Integer.parseInt(split[5]);
            if (split.length > 6) {
                this.preferredResource = split[6];
                this.preferredStartDate = LocalDate.parse(split[7]);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to parse input", ex);
        }
    }

    public boolean willSpill() {
        return spillOver > 0;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Okr okr = (Okr) o;

        if (quarter != null ? !quarter.equals(okr.quarter) : okr.quarter != null) return false;
        if (!description.equals(okr.description)) return false;
        return team != null ? team.getName().equals(okr.team.getName()) : okr.team == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + quarter.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (team != null ? team.hashCode() : 0);
        return result;
    }
}
