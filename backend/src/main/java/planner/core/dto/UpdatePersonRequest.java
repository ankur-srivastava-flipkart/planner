package planner.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import planner.core.model.Level;

@Data
@AllArgsConstructor
public class UpdatePersonRequest {
    String email;
    public float productivity;
    public Level level;
}
