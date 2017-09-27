package planner.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by ankur.srivastava on 19/09/17.
 */
@Data
@AllArgsConstructor
public class AddOkrRequest {
   private String okr;
   private List<String> preferredResource;
   private LocalDate preferredStartDate;
}
