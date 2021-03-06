import java.util.LinkedList;

/**
 * Created by fabian on 11.02.16.
 */
public class Continent {

    private final String name;
    private final int bonus;
    private final LinkedList<Territory> members;

    public Continent(String name, int reinforcementBonus, LinkedList<Territory> members){

        this.name = name;
        this.bonus = reinforcementBonus;
        this.members = members;
    }

    public String getName(){

        return name;
    }

    public int getBonus(){

        return bonus;
    }

    public LinkedList<Territory> getMembers(){

        return members;
    }
}
