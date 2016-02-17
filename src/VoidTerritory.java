import java.awt.*;

/**
 * Created by fabian on 15.01.16.
 */
public interface VoidTerritory {

    //get the number of armies in current territory
    int getArmies();

    //decrease the number of armies in current territory by 1
    void removeArmy();

    //increase the number of armies in current territory by 1
    void addReinforcement();

    int getOccupied();

    void occupy(int player, int armies);

}
