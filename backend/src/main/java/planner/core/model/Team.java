package planner.core.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by ankur.srivastava on 26/08/17.
 */

@Entity
@Data
public class Team {
    @Id
    private String id;
    private String name;
    private Person em;

}
