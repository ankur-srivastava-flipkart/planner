package planner.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePersonRequest {
    String name;
    String email;
}
