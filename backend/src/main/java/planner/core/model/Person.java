package planner.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


/**
 * Created by ankur.srivastava on 26/08/17.
 */
@Entity
@Data
@NamedQueries({
        @NamedQuery(name = "planner.core.model.person.findAll",
                query = "select p from Person p"),
        @NamedQuery(name = "planner.core.model.person.findByName",
                query = "select p from Person p"
                        + " where p.name LIKE '%:name%'")
})
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
