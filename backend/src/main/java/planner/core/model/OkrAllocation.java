package planner.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by ankur.srivastava on 25/09/17.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OkrAllocation {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private Okr okr;
    private float daysAllocated;

    public OkrAllocation(Okr okr, float effortPlannedForCurrentWeek) {
        this.okr = okr;
        this.daysAllocated = effortPlannedForCurrentWeek;
    }
}
