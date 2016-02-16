import java.util.LinkedList;

/**
 * Created by fabian on 11.02.16.
 */
public class Continent {

    public final int reinforcementBonus;
    public final LinkedList<String> members;

    public Continent(int reinforcementBonus, LinkedList<String> members){

        this.reinforcementBonus = reinforcementBonus;
        this.members = members;
    }
}
