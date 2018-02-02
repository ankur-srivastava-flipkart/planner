package planner.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.LocalDate;
import planner.core.model.Okr;

/**
 * Created by ankur.srivastava on 04/10/17.
 */
@Data
@AllArgsConstructor
public class OkrExecutionView {
    Okr okr;
    LocalDate startDate;
    LocalDate endDate;
}
