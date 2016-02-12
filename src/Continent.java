import java.util.LinkedList;

/**
 * Created by fabian on 11.02.16.
 */
public class Continent {

    public final int reinforcementBonus;
    public final LinkedList<Territory> territories;

    public Continent(int reinforcementBonus, LinkedList<Territory> territories){

        this.reinforcementBonus = reinforcementBonus;
        this.territories = territories;
    }
}
