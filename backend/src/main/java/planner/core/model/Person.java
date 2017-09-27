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
                        + " where p.name LIKE :name")
})
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String email;
    public Float productivity;

    @Enumerated(EnumType.STRING)
    public Level level;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Person(String name, String email, Float productivity, Level level) {
        this.name = name;
        this.email = email;
        this.productivity = productivity;
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Person person = (Person) o;

        if (id != null ? !id.equals(person.id) : person.id != null) return false;
        return name != null ? name.equals(person.name) : person.name == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
