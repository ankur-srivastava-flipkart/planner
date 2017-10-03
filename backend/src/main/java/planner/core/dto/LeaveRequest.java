package planner.core.dto;

import lombok.Data;
import org.joda.time.LocalDate;

/**
 * Created by ankur.srivastava on 02/10/17.
 */
@Data
public class LeaveRequest {
    String actor;
    LocalDate weekOf;
    int numberOfDays;
}
