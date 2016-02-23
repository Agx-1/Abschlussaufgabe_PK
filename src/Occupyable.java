import java.awt.*;

/**
 * Created by fabian on 15.01.16.
 */
public interface Occupyable {

    //returns the name of the current instance
    String getName();

    //get the number of armies in current territory
    int getArmies();

    //decrease the number of armies in current territory by 1
    void removeArmy();

    //increase the number of armies in current territory by 1
    void addReinforcement();

    //get occupying player
    int getOccupied();

    //set occupying player
    void setOccupied(int player);

    //occupy this territory by 'player' with 'armies'
    void occupy(int player, int armies);

}
