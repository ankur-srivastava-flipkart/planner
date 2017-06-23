package planner.core.model;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public class Okr {

    String description;
    String jiraEpic;
    int effortinPersonDays;
    Complexity complexity;
    int priority;

    public Okr(String e) {
        try {
            String[] split = e.split(":");
            this.description = split[0];
            this.jiraEpic = split[1];
            this.effortinPersonDays = Integer.parseInt(split[2]);
            this.complexity = Complexity.valueOf(split[3]);
            this.priority = Integer.parseInt(split[4]);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to parse input", ex);
        }
    }
}
