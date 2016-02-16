import java.util.LinkedList;

/**
 * Created by fabian on 11.02.16.
 */
public class Continent {

    public final int reinforcementBonus;
    public final LinkedList<Territory> members;

    public Continent(int reinforcementBonus, LinkedList<Territory> members){

        this.reinforcementBonus = reinforcementBonus;
        this.members = members;
    }
}
