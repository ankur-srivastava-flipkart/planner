package planner.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import planner.core.model.Level;

@Data
@AllArgsConstructor
public class CreatePersonRequest {
    String name;
    String email;
    public Float productivity;
    public Level level;
}
