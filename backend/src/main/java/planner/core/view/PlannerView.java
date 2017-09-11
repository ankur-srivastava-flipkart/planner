package planner.core.view;

import io.dropwizard.views.View;
import lombok.Getter;
import planner.core.model.Planner;

/**
 * Created by ankur.srivastava on 12/09/17.
 */
@Getter
public class PlannerView extends View {
    private final Planner planner;

    public PlannerView(Planner plan1) {
        super("/planner-ui/planner.ftl");
        this.planner = plan1;
    }
}
