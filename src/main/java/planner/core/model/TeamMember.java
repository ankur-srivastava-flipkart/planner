package planner.core.model;

/**
 * Created by ankur.srivastava on 23/06/17.
 */
public enum TeamMember {

    SHIKHAR("Shikhar", 0.7F),
    AMIT("Amit", 0.7F),
    ALI("Ali", 0.4F),
    MEGHA("Megha", 0.8F),
    SURYA("Surya", 0.8F),
    KIRAN("Kiran", 0.8F),
    SHRIDHAR("Shridhar", 0.8F),
    NANDHA("Nandha", 0.8F),
    VIVEK("Vivek", 0.07F),
    TEJESWAR("TEJESWAR",0.7F);


   private String name;
    private float productivity;

     TeamMember(String name, float productivity) {
         this.name = name;
         this.productivity = productivity;
     }



}
