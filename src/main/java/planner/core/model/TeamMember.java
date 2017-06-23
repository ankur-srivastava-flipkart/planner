package planner.core.model;

import static planner.core.model.Level.*;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public enum TeamMember {

    SHIKHAR("Shikhar", 0.7F, SDE2),
    AMIT("Amit", 0.7F, SDE2),
    ALI("Ali", 0.5F, SDE3),
    MEGHA("Megha", 0.8F, SDE1),
    SURYA("Surya", 0.8F, SDE1),
    KIRAN("Kiran", 0.5F, PSE2),
    SHRIDHAR("Shridhar", 0.5F, PSE2),
    NANDHA("Nandha", 0.8F, SDE1),
    VIVEK("Vivek", 0.07F, SDE2),
    TEJESWAR("TEJESWAR",0.7F, SDE2);

    public String name;
    public float productivity;
    public Level level;

     TeamMember(String name, float productivity, Level level) {
         this.name = name;
         this.productivity = productivity;
         this.level = level;
     }



}
